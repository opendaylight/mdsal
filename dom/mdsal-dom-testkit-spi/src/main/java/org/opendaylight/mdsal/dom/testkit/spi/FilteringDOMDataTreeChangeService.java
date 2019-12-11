/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMServiceTestKit.DOMTreeChangeListenerClassifier;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMServiceTestKit.DTCLPolicy;
import org.opendaylight.mdsal.dom.testkit.spi.CapturingDOMDataBroker.EventPredicate;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

final class FilteringDOMDataTreeChangeService implements DOMDataTreeChangeService {
    @GuardedBy("this")
    private final Deque<CapturedDataTreeChangeEvent> events = new ArrayDeque<>();
    private final DOMDataTreeChangeService delegate;
    private final DOMTreeChangeListenerClassifier classifier;

    FilteringDOMDataTreeChangeService(final DOMDataTreeChangeService delegate, final DOMTreeChangeListenerClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        this.classifier = requireNonNull(classifier);
    }

    @Override
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerDataTreeChangeListener(
            final DOMDataTreeIdentifier treeId, final L listener) {
        final DTCLPolicy policy = classifier.policyFor(treeId, listener);
        if (DTCLPolicy.PASS.equals(policy)) {
            return delegate.registerDataTreeChangeListener(treeId, listener);
        } else if (DTCLPolicy.IGNORE.equals(policy)) {
            return NoOpListenerRegistration.of(listener);
        } else if (policy instanceof DTCLPolicy.Capture) {
            final Object marker = ((DTCLPolicy.Capture) policy).marker;
            final ListenerRegistration<?> reg = delegate.registerDataTreeChangeListener(treeId,
                listener instanceof ClusteredDOMDataTreeChangeListener
                    ? new CapturingClusteredDTCListener(listener, marker)
                            : new CapturingDTCListener(listener, marker));

            return new AbstractListenerRegistration<>(listener) {
                @Override
                protected void removeRegistration() {
                    reg.close();
                }
            };

        }

        throw new IllegalStateException("Unhandled policy " + policy);
    }


    synchronized void addEvent(final CapturedDataTreeChangeEvent event) {
        events.add(event);
    }

    void fireCapturedEvents(final EventPredicate predicate) {
        // We need to deal with reentrancy on events, as firing an event may cause multiple other events to be enqueued
        // which will break iteration for no good reason. Those events will be executing on this thread, so synchronized
        // is not really an option.
        //
        // To avoid that scenario we do a slight twist on the Deque:
        // - we first consume all events from the queue (under synchronized to preemnt other threads) to a local temp
        // - we drop the lock and process all events, removing them as we go
        // - we re-acquire the lock and push any remaining events back (to front of the queue)
        //
        // This involves a few efficiency shenanigans, where we invert the order of events while filling remaining
        // events.

        final List<CapturedDataTreeChangeEvent> satb;
        synchronized(this) {
            // Oldest event first here ...
            satb = new ArrayList<>(events);
            events.clear();
        }

        // .. becomes newest event first here ...
        final Deque<CapturedDataTreeChangeEvent> unaccepted = new ArrayDeque<>();
        for (CapturedDataTreeChangeEvent event : satb) {
            if (predicate.test(event.marker)) {
                event.fire();
            } else {
                unaccepted.addFirst(event);
            }
        }

        synchronized (this) {
            // ... ends up being pushed to front with consecutively older events
            unaccepted.forEach(events::addFirst);
        }
    }

    private class CapturingDTCListener implements DOMDataTreeChangeListener {
        private final @NonNull DOMDataTreeChangeListener listener;
        private final @NonNull Object marker;

        CapturingDTCListener(final DOMDataTreeChangeListener listener, final Object marker) {
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        @Override
        public final void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
            addEvent(new DataTreeChanged(listener, marker, changes));
        }

        @Override
        public final void onInitialData() {
            addEvent(new InitialData(listener, marker));
        }
    }

    private final class CapturingClusteredDTCListener extends CapturingDTCListener
            implements ClusteredDOMDataTreeChangeListener {

        CapturingClusteredDTCListener(final DOMDataTreeChangeListener listener, final Object marker) {
            super(listener, marker);
        }
    }

    private abstract static class CapturedDataTreeChangeEvent {
        final @NonNull DOMDataTreeChangeListener listener;
        final @NonNull Object marker;

        CapturedDataTreeChangeEvent(final DOMDataTreeChangeListener listener, final Object marker) {
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        abstract void fire();
    }

    private static final class InitialData extends CapturedDataTreeChangeEvent {
        InitialData(final DOMDataTreeChangeListener listener, final Object marker) {
            super(listener, marker);
        }

        @Override
        void fire() {
            listener.onInitialData();
        }
    }

    private static final class DataTreeChanged extends CapturedDataTreeChangeEvent {
        private final ImmutableList<DataTreeCandidate> changes;

        DataTreeChanged(final DOMDataTreeChangeListener listener, final Object marker,
                final Collection<DataTreeCandidate> changes) {
            super(listener, marker);
            // Do not trust the collection
            this.changes = ImmutableList.copyOf(changes);
        }

        @Override
        void fire() {
            listener.onDataTreeChanged(changes);
        }
    }
}
