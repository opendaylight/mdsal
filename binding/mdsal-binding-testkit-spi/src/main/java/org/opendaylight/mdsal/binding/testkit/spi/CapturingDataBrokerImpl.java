/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.spi.ForwardingDataBroker;
import org.opendaylight.mdsal.binding.testkit.spi.AbstractTestKit.ListenerClassifier;
import org.opendaylight.mdsal.binding.testkit.spi.AbstractTestKit.ListenerEvent;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit.ListenerPolicy;
import org.opendaylight.mdsal.dom.testkit.spi.EventQueue;
import org.opendaylight.mdsal.dom.testkit.spi.EventQueue.EventPredicate;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class CapturingDataBrokerImpl<D extends DataBroker> extends ForwardingDataBroker implements CapturingDataBroker {
    private final EventQueue<ListenerEvent> events = new EventQueue<>();
    private final ListenerClassifier classifier;
    private final D delegate;

    CapturingDataBrokerImpl(final D delegate, final ListenerClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        this.classifier = requireNonNull(classifier);
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>>
            ListenerRegistration<L> registerDataTreeChangeListener(final DataTreeIdentifier<T> treeId,
                    final L listener) {
        final ListenerPolicy policy = classifier.policyFor(treeId, listener);
        if (ListenerPolicy.PASS.equals(policy)) {
            return delegate.registerDataTreeChangeListener(treeId, listener);
        } else if (ListenerPolicy.IGNORE.equals(policy)) {
            return NoOpListenerRegistration.of(listener);
        } else if (policy instanceof ListenerPolicy.Capture) {
            final Object marker = ((ListenerPolicy.Capture) policy).getMarker();
            final ListenerRegistration<?> reg = delegate.registerDataTreeChangeListener(treeId,
                listener instanceof ClusteredDataTreeChangeListener
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


    @Override
    public void fireCapturedEvents(final EventPredicate predicate) {
        events.consumeEvents(event -> {
            if (predicate.test(event.marker())) {
                event.fire();
                return true;
            }
            return false;
        });
    }

    @Override
    protected D delegate() {
        return delegate;
    }

    private static class DTCListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private final DataTreeChangeListener<T> listener;
        private final EventQueue<ListenerEvent> sink;
        private final Object marker;

        DTCListener(final EventQueue<ListenerEvent> sink, final DataTreeChangeListener<T> listener,
                final Object marker) {
            this.sink = requireNonNull(sink);
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
            sink.addEvent(new DataTreeChanged<>(listener, marker, changes));
        }

        @Override
        public final void onInitialData() {
            sink.addEvent(new InitialData(listener, marker));
        }
    }

    private static final class ClusteredDTCListener<T extends DataObject> extends DTCListener<T>
            implements ClusteredDataTreeChangeListener<T> {
        ClusteredDTCListener(final EventQueue<ListenerEvent> sink, final DataTreeChangeListener<T> listener,
                final Object marker) {
            super(sink, listener, marker);
        }
    }

    private static final class InitialData extends ListenerEvent {
        InitialData(final DataTreeChangeListener<?> listener, final Object marker) {
            super(listener, marker);
        }

        @Override
        void fire() {
            listener().onInitialData();
        }
    }

    @NonNullByDefault
    private static final class DataTreeChanged<T extends DataObject> extends ListenerEvent {
        private final ImmutableList<DataTreeModification<T>> changes;

        DataTreeChanged(final DataTreeChangeListener<T> listener, final Object marker,
                final Collection<DataTreeModification<T>> changes) {
            super(listener, marker);
            // Do not trust the collection
            this.changes = ImmutableList.copyOf(changes);
        }

        @Override
        void fire() {
            ((DataTreeChangeListener<T>) listener()).onDataTreeChanged(changes);
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("changes", changes);
        }
    }
}
