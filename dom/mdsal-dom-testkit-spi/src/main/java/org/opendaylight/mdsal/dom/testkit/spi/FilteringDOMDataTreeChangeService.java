/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit.DOMListenerClassifier;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit.DOMListenerEvent;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit.ListenerPolicy;
import org.opendaylight.mdsal.dom.testkit.spi.EventQueue.EventPredicate;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

final class FilteringDOMDataTreeChangeService implements DOMDataTreeChangeService {
    private final EventQueue<DOMListenerEvent> events = new EventQueue<>();
    private final DOMDataTreeChangeService delegate;
    private final DOMListenerClassifier classifier;

    FilteringDOMDataTreeChangeService(final DOMDataTreeChangeService delegate,
            final DOMListenerClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        this.classifier = requireNonNull(classifier);
    }

    @Override
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerDataTreeChangeListener(
            final DOMDataTreeIdentifier treeId, final L listener) {
        final ListenerPolicy policy = classifier.policyFor(treeId, listener);
        if (ListenerPolicy.PASS.equals(policy)) {
            return delegate.registerDataTreeChangeListener(treeId, listener);
        } else if (ListenerPolicy.IGNORE.equals(policy)) {
            return NoOpListenerRegistration.of(listener);
        } else if (policy instanceof ListenerPolicy.Capture) {
            final Object marker = ((ListenerPolicy.Capture) policy).getMarker();
            final ListenerRegistration<?> reg = delegate.registerDataTreeChangeListener(treeId,
                listener instanceof ClusteredDOMDataTreeChangeListener
                    ? new ClusteredDTCListener(events, listener, marker)
                            : new DTCListener(events, listener, marker));

            return new AbstractListenerRegistration<>(listener) {
                @Override
                protected void removeRegistration() {
                    reg.close();
                }
            };

        }

        throw new IllegalStateException("Unhandled policy " + policy);
    }

    void fireCapturedEvents(final EventPredicate predicate) {
        events.consumeEvents(event -> {
            if (predicate.test(event.marker())) {
                event.fire();
                return true;
            }
            return false;
        });
    }

    @NonNullByDefault
    private static class DTCListener implements DOMDataTreeChangeListener {
        private final DOMDataTreeChangeListener listener;
        private final EventQueue<DOMListenerEvent> sink;
        private final Object marker;

        DTCListener(final EventQueue<DOMListenerEvent> sink, final DOMDataTreeChangeListener listener,
                final Object marker) {
            this.sink = requireNonNull(sink);
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        @Override
        public final void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
            sink.addEvent(new DataTreeChanged(listener, marker, changes));
        }

        @Override
        public final void onInitialData() {
            sink.addEvent(new InitialData(listener, marker));
        }
    }

    private static final class ClusteredDTCListener extends DTCListener implements ClusteredDOMDataTreeChangeListener {
        ClusteredDTCListener(final EventQueue<DOMListenerEvent> sink, final DOMDataTreeChangeListener listener,
                final Object marker) {
            super(sink, listener, marker);
        }
    }

    @NonNullByDefault
    private static final class InitialData extends DOMListenerEvent {
        InitialData(final DOMDataTreeChangeListener listener, final Object marker) {
            super(listener, marker);
        }

        @Override
        void fire() {
            listener().onInitialData();
        }
    }

    @NonNullByDefault
    private static final class DataTreeChanged extends DOMListenerEvent {
        private final ImmutableList<DataTreeCandidate> changes;

        DataTreeChanged(final DOMDataTreeChangeListener listener, final Object marker,
                final Collection<DataTreeCandidate> changes) {
            super(listener, marker);
            // Do not trust the collection
            this.changes = ImmutableList.copyOf(changes);
        }

        @Override
        void fire() {
            listener().onDataTreeChanged(changes);
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("changes", changes);
        }
    }
}
