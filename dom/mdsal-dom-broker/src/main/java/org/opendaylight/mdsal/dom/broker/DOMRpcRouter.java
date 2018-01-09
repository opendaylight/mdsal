/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMImplementation;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationCallback;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderService;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.AbstractDOMOperationImplementationRegistration;
import org.opendaylight.mdsal.dom.spi.AbstractDOMRpcImplementationRegistration;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class DOMRpcRouter implements AutoCloseable, DOMOperationService, DOMRpcService,
        DOMRpcProviderService, DOMOperationProviderService, SchemaContextListener {
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(
            "DOMRpcRouter-listener-%s").setDaemon(true).build();

    private final ExecutorService listenerNotifier = Executors.newSingleThreadExecutor(THREAD_FACTORY);

    @GuardedBy("this")
    private Collection<AbstractOperationRegistration<? extends EventListener, ? extends DOMImplementation>> listeners
        = Collections.emptyList();

    private volatile DOMRpcRoutingTable routingTable = DOMRpcRoutingTable.EMPTY;

    private ListenerRegistration<?> listenerRegistration;

    public static DOMRpcRouter newInstance(final DOMSchemaService schemaService) {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        rpcRouter.listenerRegistration = schemaService.registerSchemaContextListener(rpcRouter);
        return rpcRouter;
    }

    @Deprecated
    @Override
    public <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T> registerRpcImplementation(
            final T implementation, final DOMRpcIdentifier... rpcs) {
        return registerRpcImplementation(implementation, ImmutableSet.copyOf(rpcs));
    }

    @Deprecated
    @Override
    public synchronized <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T>
            registerRpcImplementation(final T implementation, final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcImplementationAdapter adapterImpl = new DOMRpcImplementationAdapter(implementation);
        final DOMRpcRoutingTable newTable =
            registerImplementation(adapterImpl, rpcs);

        listenerNotifier.execute(() -> notifyAdded(newTable, (DOMRpcImplementation) adapterImpl));

        return new AbstractDOMRpcImplementationRegistration<T>(implementation) {
            @Override
            protected void removeRegistration() {
                removeRpcImplementation(getInstance(), rpcs);
            }
        };
    }

    @Nonnull
    @Override
    public <T extends DOMOperationImplementation> DOMOperationImplementationRegistration<T>
            registerOperationImplementation(@Nonnull final T implementation, @Nonnull final DOMRpcIdentifier... rpcs) {
        return registerOperationImplementation(implementation, ImmutableSet.copyOf(rpcs));
    }

    @Nonnull
    @Override
    public synchronized <T extends DOMOperationImplementation> DOMOperationImplementationRegistration<T>
            registerOperationImplementation(@Nonnull final T implementation, @Nonnull final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcRoutingTable newTable =
            registerImplementation(implementation, rpcs);

        listenerNotifier.execute(() -> notifyAdded(newTable, implementation));

        return new AbstractDOMOperationImplementationRegistration<T>(implementation) {
            @Override
            protected void removeRegistration() {
                removeOperationImplementation(getInstance(), rpcs);
            }
        };
    }

    private DOMRpcRoutingTable registerImplementation(final DOMOperationImplementation implementation,
                                                      final Set<DOMRpcIdentifier> operations) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.add(implementation, operations);
        routingTable = newTable;
        return newTable;
    }

    @Deprecated
    private synchronized void removeRpcImplementation(final DOMRpcImplementation implementation,
                                                      final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.remove((DOMOperationImplementation) implementation, rpcs);
        routingTable = newTable;

        listenerNotifier.execute(() -> notifyRemoved(newTable, implementation));
    }

    private synchronized void removeOperationImplementation(final DOMOperationImplementation implementation,
                                                      final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.remove(implementation, rpcs);
        routingTable = newTable;

        listenerNotifier.execute(() -> notifyRemoved(newTable, implementation));
    }


    @Override
    public void invokeRpc(final SchemaPath type, final NormalizedNode<?, ?> input, final DOMOperationCallback callback,
            final Executor callbackExecutor) {
        routingTable.invokeRpc(type, input, callback);
    }

    @Override
    public void invokeAction(final SchemaPath type, final YangInstanceIdentifier parent,
            final NormalizedNode<?, ?> input, final DOMOperationCallback callback, final Executor callbackExecutor) {
        routingTable.invokeAction(type, parent, input, callback);
    }

    @Nonnull
    @Override
    public synchronized <T extends @NonNull DOMOperationAvailabilityListener> ListenerRegistration<T>
            registerOperationListener(@Nonnull final T listener) {
        final OperationRegistration<T> ret = new OperationRegistration<>(this, listener);
        final Builder<AbstractOperationRegistration<?, ?>> b = ImmutableList.builder();
        b.addAll(listeners);
        b.add(ret);
        listeners = b.build();

        listenerNotifier.execute(() -> ret.initialTable(routingTable));
        return ret;
    }

    private synchronized void removeListener(final ListenerRegistration<? extends EventListener> reg) {
        listeners = ImmutableList.copyOf(Collections2.filter(listeners, input -> !reg.equals(input)));
    }

    @Deprecated
    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable,
            final DOMRpcImplementation impl) {
        for (AbstractOperationRegistration<?, ?> listener : listeners) {
            if (listener instanceof Registration) {
                ((Registration) listener).addOperation(newTable, impl);
            }

            if (listener instanceof OperationRegistration) {
                ((OperationRegistration) listener).addOperation(newTable, (DOMOperationImplementation) impl);
            }
        }

    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable, final DOMOperationImplementation impl) {
        for (AbstractOperationRegistration<?, ?> listener : listeners) {
            if (listener instanceof Registration) {
                ((OperationRegistration) listener).addOperation(newTable, impl);
            }
        }

    }


    @Deprecated
    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable,
                                            final DOMRpcImplementation impl) {
        for (AbstractOperationRegistration<?, ?> listener : listeners) {
            if (listener instanceof Registration) {
                ((Registration) listener).removeOperation(newTable, impl);
            }

            if (listener instanceof OperationRegistration) {
                ((OperationRegistration) listener).removeOperation(newTable, (DOMOperationImplementation) impl);
            }
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable, final DOMOperationImplementation impl) {
        for (AbstractOperationRegistration<?, ?> listener : listeners) {
            if (listener instanceof OperationRegistration) {
                ((OperationRegistration) listener).removeOperation(newTable, impl);
            }
        }
    }

    @Override
    public synchronized <T extends DOMRpcAvailabilityListener> ListenerRegistration<T> registerRpcListener(
            final T listener) {
        final Registration<T> ret = new Registration<>(this, listener);
        final Builder<AbstractOperationRegistration<? extends EventListener, ? extends DOMImplementation>> b
            = ImmutableList.builder();
        b.addAll(listeners);
        b.add(ret);
        listeners = b.build();

        listenerNotifier.execute(() -> ret.initialTable(routingTable));
        return ret;
    }

    @Override
    public synchronized void onGlobalContextUpdated(final SchemaContext context) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.setSchemaContext(context);
        routingTable = newTable;
    }

    @Override
    public void close() {
        listenerNotifier.shutdown();

        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    @VisibleForTesting
    Collection<?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    DOMRpcRoutingTable routingTable() {
        return routingTable;
    }

    private static final class Registration<T extends DOMRpcAvailabilityListener>
        extends AbstractOperationRegistration<T, DOMRpcImplementation> {

        Registration(final DOMRpcRouter router, final T listener) {
            super(router, listener);
        }

        @Override
        void initialTable(final DOMRpcRoutingTable newTable) {
            final T listener = getInstance();
            if (listener == null) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs = newTable.getRpcs(listener);
            final Collection<DOMRpcIdentifier> added = getAddedOperations(rpcs);
            if (!added.isEmpty()) {
                listener.onRpcAvailable(added);
            }
        }

        @Override
        void addOperation(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            final T listener = getInstance();
            if (listener == null || !listener.acceptsImplementation(impl)) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs = newTable.getRpcs(listener);
            final Collection<DOMRpcIdentifier> added = getAddedOperations(rpcs);

            if (!added.isEmpty()) {
                listener.onRpcAvailable(added);
            }
        }

        @Override
        void removeOperation(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            final T listener = getInstance();
            if (listener == null || !listener.acceptsImplementation(impl)) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs = newTable.getRpcs(listener);
            final Collection<DOMRpcIdentifier> removed = getRemovededOperations(rpcs);

            if (!removed.isEmpty()) {
                listener.onRpcUnavailable(removed);
            }
        }
    }

    private static final class OperationRegistration<T extends DOMOperationAvailabilityListener>
            extends AbstractOperationRegistration<T, DOMOperationImplementation> {

        OperationRegistration(final DOMRpcRouter router, final T listener) {
            super(router, listener);
        }

        @Override
        void initialTable(final DOMRpcRoutingTable newTable) {
            final T listener = getInstance();
            if (listener == null) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> oprs = newTable.getOperations(listener);
            final Collection<DOMRpcIdentifier> added = getAddedOperations(oprs);
            if (!added.isEmpty()) {
                listener.onOperationAvailable(added);
            }
        }

        @Override
        void addOperation(final DOMRpcRoutingTable newTable, final DOMOperationImplementation impl) {
            final T listener = getInstance();
            if (listener == null || !listener.acceptsImplementation(impl)) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> oprs = newTable.getOperations(listener);

            final Collection<DOMRpcIdentifier> added = getAddedOperations(oprs);
            if (!added.isEmpty()) {
                listener.onOperationAvailable(added);
            }
        }

        @Override
        void removeOperation(final DOMRpcRoutingTable newTable, final DOMOperationImplementation impl) {
            final T listener = getInstance();
            if (listener == null || !listener.acceptsImplementation(impl)) {
                return;
            }

            final Map<SchemaPath, Set<YangInstanceIdentifier>> oprs = newTable.getOperations(listener);

            final Collection<DOMRpcIdentifier> removed = getRemovededOperations(oprs);

            if (!removed.isEmpty()) {
                listener.onOperationUnavailable(removed);
            }
        }
    }

    private abstract static class AbstractOperationRegistration<T extends EventListener, I extends DOMImplementation>
            extends AbstractListenerRegistration<T> {

        private final DOMRpcRouter router;

        private Map<SchemaPath, Set<YangInstanceIdentifier>> prevRpcs;

        AbstractOperationRegistration(final DOMRpcRouter router, final T listener) {
            super(listener);
            this.router = router;
        }

        private void setPrevRpcs(final Map<SchemaPath, Set<YangInstanceIdentifier>> prevRpcs) {
            this.prevRpcs = prevRpcs;
        }

        private Map<SchemaPath, Set<YangInstanceIdentifier>> getPrevRpcs() {
            return prevRpcs;
        }

        final Collection<DOMRpcIdentifier> getAddedOperations(final Map<SchemaPath, Set<YangInstanceIdentifier>> newOprs) {
            final MapDifference<SchemaPath, Set<YangInstanceIdentifier>> diff = Maps.difference(getPrevRpcs(), newOprs);

            final Collection<DOMRpcIdentifier> added = new ArrayList<>();
            for (Entry<SchemaPath, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), yii -> DOMRpcIdentifier.create(e.getKey(), yii)));
            }
            for (Entry<SchemaPath, ValueDifference<Set<YangInstanceIdentifier>>> e :
                diff.entriesDiffering().entrySet()) {
                for (YangInstanceIdentifier yii
                        : Sets.difference(e.getValue().rightValue(), e.getValue().leftValue())) {
                    added.add(DOMRpcIdentifier.create(e.getKey(), yii));
                }
            }

            setPrevRpcs(newOprs);

            return added;
        }

        final Collection<DOMRpcIdentifier> getRemovededOperations(
                final Map<SchemaPath, Set<YangInstanceIdentifier>> newOprs) {
            final MapDifference<SchemaPath, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, newOprs);

            final Collection<DOMRpcIdentifier> removed = new ArrayList<>();
            for (Entry<SchemaPath, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), yii -> DOMRpcIdentifier.create(e.getKey(), yii)));
            }
            for (Entry<SchemaPath, ValueDifference<Set<YangInstanceIdentifier>>> e :
                diff.entriesDiffering().entrySet()) {
                for (YangInstanceIdentifier yii
                        : Sets.difference(e.getValue().leftValue(), e.getValue().rightValue())) {
                    removed.add(DOMRpcIdentifier.create(e.getKey(), yii));
                }
            }

            setPrevRpcs(newOprs);

            return removed;
        }

        @Override
        protected void removeRegistration() {
            router.removeListener(this);
        }

        abstract void initialTable(DOMRpcRoutingTable newTable);

        abstract void addOperation(DOMRpcRoutingTable newTable, I impl);

        abstract void removeOperation(DOMRpcRoutingTable newTable, I impl);
    }
}
