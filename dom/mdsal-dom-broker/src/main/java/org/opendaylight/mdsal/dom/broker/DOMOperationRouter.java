/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityExtension;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance.Action;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance.Rpc;
import org.opendaylight.mdsal.dom.api.DOMOperationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderService;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMOperationServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTable.DOMActionRoutingTable;
import org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.DOMActionRoutingTableEntry;
import org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.DOMRpcRoutingTableEntry;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class DOMOperationRouter extends AbstractRegistration implements SchemaContextListener {
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(
            "DOMOperationRouter-listener-%s").setDaemon(true).build();

    private final ExecutorService listenerNotifier = Executors.newSingleThreadExecutor(THREAD_FACTORY);
    private final DOMOperationProviderService operationProviderService = new OperationProviderServiceFacade();
    private final DOMOperationService operationService = new OperationServiceFacade();

    @GuardedBy("this")
    private Collection<Registration<?>> listeners = Collections.emptyList();

    private volatile DOMOperationRoutingTable.DOMRpcRoutingTable rpcRoutingTable =
        DOMOperationRoutingTable.DOMRpcRoutingTable.EMPTY;
    private volatile DOMActionRoutingTable actionRoutingTable =
        DOMActionRoutingTable.EMPTY;

    private ListenerRegistration<?> listenerRegistration;

    public static DOMOperationRouter newInstance(final DOMSchemaService schemaService) {
        final DOMOperationRouter operationRouter = new DOMOperationRouter();
        operationRouter.listenerRegistration = schemaService.registerSchemaContextListener(operationRouter);
        return operationRouter;
    }

    public DOMOperationService getOperationService() {
        return operationService;
    }

    public DOMOperationProviderService getOperationProviderService() {
        return operationProviderService;
    }

    private synchronized void removeRpcImplementation(final DOMOperationImplementation.Rpc implementation,
            final Set<DOMOperationInstance.Rpc> rpcs) {
        final DOMOperationRoutingTable.DOMRpcRoutingTable oldTable = rpcRoutingTable;
        final DOMOperationRoutingTable.DOMRpcRoutingTable newTable = oldTable.remove(implementation, rpcs);
        rpcRoutingTable = newTable;

        listenerNotifier.execute(() -> notifyRpcChanged(newTable, implementation));
    }

    private synchronized void removeActionImplementation(final DOMOperationImplementation.Action implementation,
            final Set<DOMOperationInstance.Action> actions) {
        final DOMOperationRoutingTable.DOMActionRoutingTable oldTable = actionRoutingTable;
        final DOMOperationRoutingTable.DOMActionRoutingTable newTable = oldTable.remove(implementation, actions);
        actionRoutingTable = newTable;

        listenerNotifier.execute(() -> notifyActionChanged(newTable, implementation));
    }

    private synchronized void removeListener(final ListenerRegistration<? extends AvailabilityListener> reg) {
        listeners = ImmutableList.copyOf(Collections2.filter(listeners, input -> !reg.equals(input)));
    }

    private synchronized void notifyRpcChanged(final DOMOperationRoutingTable.DOMRpcRoutingTable newTable,
            final DOMOperationImplementation.Rpc impl) {
        for (Registration<?> l : listeners) {
            l.rpcChanged(newTable, impl);
        }
    }

    private synchronized void notifyActionChanged(final DOMOperationRoutingTable.DOMActionRoutingTable newTable,
            final DOMOperationImplementation.Action impl) {
        for (Registration<?> l : listeners) {
            l.actionChanged(newTable, impl);
        }
    }

    @Override
    public synchronized void onGlobalContextUpdated(final SchemaContext context) {
        final DOMOperationRoutingTable.DOMRpcRoutingTable oldRpcTable = rpcRoutingTable;
        rpcRoutingTable = oldRpcTable.setSchemaContext(context);

        final DOMOperationRoutingTable.DOMActionRoutingTable oldActionTable = actionRoutingTable;
        actionRoutingTable = oldActionTable.setSchemaContext(context);
    }

    @Override
    protected void removeRegistration() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
        listenerNotifier.shutdown();
    }

    @VisibleForTesting
    Collection<?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    DOMOperationRoutingTable.DOMRpcRoutingTable rpcRoutingTable() {
        return rpcRoutingTable;
    }

    @VisibleForTesting
    DOMOperationRoutingTable.DOMActionRoutingTable actionRoutingTable() {
        return actionRoutingTable;
    }

    private final class Registration<T extends AvailabilityListener>
        extends AbstractListenerRegistration<T> {
        private Map<QName, Set<YangInstanceIdentifier>> prevRpcs;
        private Map<SchemaPath, Set<DOMDataTreeIdentifier>> prevActions;
        private DOMOperationRouter router;

        Registration(final DOMOperationRouter router, final T listener,
                Map<QName, Set<YangInstanceIdentifier>> rpcs, Map<SchemaPath, Set<DOMDataTreeIdentifier>> actions) {
            super(listener);
            this.router = requireNonNull(router);
            this.prevRpcs = requireNonNull(rpcs);
            this.prevActions = requireNonNull(actions);
        }

        void initialTable() {
            final Set<DOMOperationInstance<?>> added = new HashSet<>();

            for (Entry<QName, Set<YangInstanceIdentifier>> e : prevRpcs.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(),
                    i -> DOMOperationInstance.rpcOf(e.getKey())));
            }

            for (Entry<SchemaPath, Set<DOMDataTreeIdentifier>> e : prevActions.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(),
                    i -> DOMOperationInstance.actionOf(e.getKey(), e.getValue())));
            }
            if (!added.isEmpty()) {
                getInstance().onOperationsChanged(new HashSet<>(), added);
            }
        }

        @Override
        protected void removeRegistration() {
            router.removeListener(this);
            router = null;
        }

        void rpcChanged(final DOMOperationRoutingTable.DOMRpcRoutingTable newTable,
                final DOMOperationImplementation.Rpc impl) {
            final T l = getInstance();
            if (!l.acceptsImplementation(impl)) {
                return;
            }

            final Map<QName, Set<YangInstanceIdentifier>> rpcs = verifyNotNull(newTable.getOperations(l));
            final MapDifference<QName, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, rpcs);

            final Set<DOMOperationInstance<?>> added = new HashSet<>();
            final Set<DOMOperationInstance<?>> removed = new HashSet<>();
            for (Entry<QName, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), i -> DOMOperationInstance.rpcOf(e.getKey())));
            }

            for (Entry<QName, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMOperationInstance.rpcOf(e.getKey())));
            }
            //There're no routed rpc at all ,so the key is always YangInstanceIdentifier.EMPTY
            //and then it's no need for RPC to do 'Sets.difference' whereas Action should.
            prevRpcs = rpcs;
            if (!added.isEmpty() || !removed.isEmpty()) {
                l.onOperationsChanged(removed, added);
            }
        }

        void actionChanged(final DOMOperationRoutingTable.DOMActionRoutingTable newTable,
                final DOMOperationImplementation.Action impl) {
            final T l = getInstance();
            if (!l.acceptsImplementation(impl)) {
                return;
            }

            final Map<SchemaPath, Set<DOMDataTreeIdentifier>> actions = verifyNotNull(newTable.getOperations(l));
            final MapDifference<SchemaPath, Set<DOMDataTreeIdentifier>> diff = Maps.difference(prevActions, actions);

            final Set<DOMOperationInstance<?>> added = new HashSet<>();
            final Set<DOMOperationInstance<?>> removed = new HashSet<>();
            for (Entry<SchemaPath, Set<DOMDataTreeIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(),
                    i -> DOMOperationInstance.actionOf(e.getKey(), i)));
            }

            for (Entry<SchemaPath, Set<DOMDataTreeIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMOperationInstance.actionOf(e.getKey(), i)));
            }

            for (Entry<SchemaPath, ValueDifference<Set<DOMDataTreeIdentifier>>> e :
                    diff.entriesDiffering().entrySet()) {
                for (DOMDataTreeIdentifier i : Sets.difference(e.getValue().rightValue(), e.getValue().leftValue())) {
                    added.add(DOMOperationInstance.actionOf(e.getKey(), i));
                }

                for (DOMDataTreeIdentifier i : Sets.difference(e.getValue().leftValue(), e.getValue().rightValue())) {
                    removed.add(DOMOperationInstance.actionOf(e.getKey(), i));
                }
            }

            prevActions = actions;
            if (!added.isEmpty() || !removed.isEmpty()) {
                l.onOperationsChanged(removed, added);
            }
        }
    }

    private final class OperationProviderServiceFacade implements DOMOperationProviderService {
        private final ClassToInstanceMap<DOMOperationProviderServiceExtension> extensions;

        private OperationProviderServiceFacade() {
            //FIXME: initiate extensions correctly
            this.extensions = ImmutableClassToInstanceMap.of();
        }

        @Override
        public <T extends DOMOperationImplementation.Action> ObjectRegistration<T> registerActionImplementation(
                T implementation, Set<Action> instances) {
            synchronized (DOMOperationRouter.this) {
                final DOMActionRoutingTable oldTable = actionRoutingTable;
                actionRoutingTable = oldTable.add(implementation, instances);

                listenerNotifier.execute(() -> notifyActionChanged(actionRoutingTable, implementation));
            }

            return new AbstractObjectRegistration<T>(implementation) {
                @Override
                protected void removeRegistration() {
                    removeActionImplementation(getInstance(), instances);
                }
            };
        }

        @Override
        public <T extends DOMOperationImplementation.Rpc> ObjectRegistration<T> registerRpcImplementation(
                T implementation, Set<Rpc> instances) {
            synchronized (DOMOperationRouter.this) {
                final DOMOperationRoutingTable.DOMRpcRoutingTable oldTable = rpcRoutingTable;
                rpcRoutingTable = oldTable.add(implementation, instances);

                listenerNotifier.execute(() -> notifyRpcChanged(rpcRoutingTable, implementation));
            }

            return new AbstractObjectRegistration<T>(implementation) {
                @Override
                protected void removeRegistration() {
                    removeRpcImplementation(getInstance(), instances);
                }
            };
        }

        @Override
        public ClassToInstanceMap<DOMOperationProviderServiceExtension> getExtensions() {
            return extensions;
        }
    }

    private final class OperationServiceFacade implements DOMOperationService {
        @Override
        public FluentFuture<? extends DOMOperationResult> invokeRpc(QName type, ContainerNode input) {
            final DOMRpcRoutingTableEntry entry = rpcRoutingTable.getEntry(type);
            if (entry == null) {
                return FluentFutures.immediateFailedFluentFuture(
                    new DOMOperationNotAvailableException("No implementation of RPC %s available", type));
            }

            return entry.invokeRpc(input);
        }

        @Override
        public FluentFuture<? extends DOMOperationResult> invokeAction(SchemaPath type,
                DOMDataTreeIdentifier path, ContainerNode input) {
            final DOMActionRoutingTableEntry entry = actionRoutingTable.getEntry(type);
            if (entry == null) {
                return FluentFutures.immediateFailedFluentFuture(
                    new DOMOperationNotAvailableException("No implementation of Action %s available", type));
            }

            return entry.invokeAction(path, input);
        }

        @Override
        public ClassToInstanceMap<DOMOperationServiceExtension> getExtensions() {
            //FIXME:
            return ImmutableClassToInstanceMap.of(DOMOperationAvailabilityExtension.class,
                new DOMOperationAvailabilityExtension() {
                    @Override
                    public <T extends AvailabilityListener> ListenerRegistration<T>
                        registerAvailabilityListener(T listener) {
                        synchronized (DOMOperationRouter.this) {
                            final Registration<T> ret = new Registration<>(DOMOperationRouter.this,
                                listener, rpcRoutingTable.getOperations(listener),
                                actionRoutingTable.getOperations());
                            final Builder<Registration<?>> b = ImmutableList.builder();
                            b.addAll(listeners);
                            b.add(ret);
                            listeners = b.build();

                            listenerNotifier.execute(ret::initialTable);
                            return ret;
                        }
                    }
                });
        }
    }
}
