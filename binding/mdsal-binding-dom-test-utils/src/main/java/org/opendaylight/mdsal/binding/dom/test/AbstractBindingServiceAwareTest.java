/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.test;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.BiPredicate;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.spi.ForwardingDataBroker;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Base class for tests running with models packaged as Binding artifacts on their classpath and need to interact with
 * {@link BindingService}s. Services are lazily instantiated on-demand.
 *
 * @author Robert Varga
 */
@Beta
public abstract class AbstractBindingServiceAwareTest extends DOMServiceTestKit {

    private final Queue<CapturedDataTreeChangeEvent<?>> events = new ArrayDeque<>();

    private volatile DataBroker dataBroker;

    protected final DataBroker dataBroker() {
        DataBroker local = dataBroker;
        if (local == null) {
            synchronized (this) {
                local = dataBroker;
                if (local == null) {
                    dataBroker = local = new TestingDataBroker(createDataBroker());
                }
            }
        }
        return local;
    }

    abstract DataBroker createDataBroker();

    protected <T extends DataObject> @NonNull SubscriptionPolicy dtclPolicyFor(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        return SubscriptionPolicy.ALLOW;
    }

    protected final synchronized void runEvents(final DataTreeChangeEventPredicate predicate) {
        final Iterator<CapturedDataTreeChangeEvent<?>> it = events.iterator();
        while (it.hasNext()) {
            final CapturedDataTreeChangeEvent<?> event = it.next();
            if (predicate.test(event.registrationPath, event.listener)) {
                it.remove();
                event.fire();
            }
        }
    }

    final synchronized void addEvent(final CapturedDataTreeChangeEvent<?> event) {
        events.add(event);
    }

    @FunctionalInterface
    protected interface DataTreeChangeEventPredicate
            extends BiPredicate<DataTreeIdentifier<?>, DataTreeChangeListener<?>> {

    }

    protected enum SubscriptionPolicy {
        ALLOW,
        IGNORE,
        CAPTURE;
    }

    private abstract static class CapturedDataTreeChangeEvent<T extends DataObject> {
        final @NonNull DataTreeIdentifier<T> registrationPath;
        final @NonNull DataTreeChangeListener<T> listener;

        CapturedDataTreeChangeEvent(final DataTreeIdentifier<T> registrationPath,
                final DataTreeChangeListener<T> delegate) {
            this.registrationPath = requireNonNull(registrationPath);
            this.listener = requireNonNull(delegate);
        }

        abstract void fire();
    }

    private static final class InitialData<T extends DataObject> extends CapturedDataTreeChangeEvent<T> {
        InitialData(final DataTreeIdentifier<T> registrationPath,final DataTreeChangeListener<T> delegate) {
            super(registrationPath, delegate);
        }

        @Override
        void fire() {
            listener.onInitialData();
        }
    }

    private static final class DataTreeChanged<T extends DataObject> extends CapturedDataTreeChangeEvent<T> {
        private final ImmutableList<DataTreeModification<T>> changes;

        DataTreeChanged(final DataTreeIdentifier<T> registrationPath, final DataTreeChangeListener<T> delegate,
                final Collection<DataTreeModification<T>> changes) {
            super(registrationPath, delegate);
            this.changes = ImmutableList.copyOf(changes);
        }

        @Override
        void fire() {
            listener.onDataTreeChanged(changes);
        }
    }

    private final class TestingDataBroker extends ForwardingDataBroker {
        private final @NonNull DataBroker delegate;

        protected TestingDataBroker(final DataBroker delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        protected DataBroker delegate() {
            return delegate;
        }

        @Override
        public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
                registerDataTreeChangeListener(final DataTreeIdentifier<T> treeId, final L listener) {
            final SubscriptionPolicy policy = dtclPolicyFor(treeId, listener);
            switch (policy) {
                case IGNORE:
                    return NoOpListenerRegistration.of(listener);
                case ALLOW:
                    return super.registerDataTreeChangeListener(treeId, listener);
                case CAPTURE:
                    final ListenerRegistration<?> reg = super.registerDataTreeChangeListener(treeId,
                        listener instanceof ClusteredDataTreeChangeListener
                            ? new CapturingClusteredDTCListener<>(treeId, listener)
                                    : new CapturingDTCListener<>(treeId, listener));

                    return new AbstractListenerRegistration<>(listener) {
                        @Override
                        protected void removeRegistration() {
                            reg.close();
                        }
                    };
                default:
                    throw new IllegalStateException("Unhandled policy " + policy);
            }
        }
    }

    private class CapturingDTCListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private final @NonNull DataTreeIdentifier<T> path;
        private final @NonNull DataTreeChangeListener<T> delegate;

        CapturingDTCListener(final DataTreeIdentifier<T> path, final DataTreeChangeListener<T> delegate) {
            this.path = requireNonNull(path);
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public final void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
            addEvent(new DataTreeChanged<>(path, delegate, changes));
        }

        @Override
        public final void onInitialData() {
            addEvent(new InitialData<>(path, delegate));
        }
    }

    private final class CapturingClusteredDTCListener<T extends DataObject> extends CapturingDTCListener<T>
            implements ClusteredDataTreeChangeListener<T> {

        CapturingClusteredDTCListener(final DataTreeIdentifier<T> path, final DataTreeChangeListener<T> delegate) {
            super(path, delegate);
        }
    }
}
