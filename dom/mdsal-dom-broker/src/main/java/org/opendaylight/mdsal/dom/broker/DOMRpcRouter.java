/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMActionServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.AbstractDOMRpcImplementationRegistration;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, service = DOMRpcRouterServices.class)
@RequireServiceComponentRuntime
public final class DOMRpcRouter extends AbstractRegistration
        implements DOMRpcRouterServices, EffectiveModelContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(DOMRpcRouter.class);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(
            "DOMRpcRouter-listener-%s").setDaemon(true).build();

    private final ExecutorService listenerNotifier = Executors.newSingleThreadExecutor(THREAD_FACTORY);
    private final @NonNull DOMActionProviderService actionProviderService = new ActionProviderServiceFacade();
    private final @NonNull DOMActionService actionService = new ActionServiceFacade();
    private final @NonNull DOMRpcProviderService rpcProviderService = new RpcProviderServiceFacade();
    private final @NonNull DOMRpcService rpcService = new RpcServiceFacade();

    @GuardedBy("this")
    private ImmutableList<Registration<?>> listeners = ImmutableList.of();

    @GuardedBy("this")
    private ImmutableList<ActionRegistration<?>> actionListeners = ImmutableList.of();

    private volatile DOMRpcRoutingTable routingTable = DOMRpcRoutingTable.EMPTY;

    private volatile DOMActionRoutingTable actionRoutingTable = DOMActionRoutingTable.EMPTY;

    private ListenerRegistration<?> listenerRegistration;

    @Deprecated
    @VisibleForTesting
    // FIXME: 9.0.0: make this constructor package-private
    public DOMRpcRouter() {

    }

    @Inject
    @Activate
    public DOMRpcRouter(@Reference final DOMSchemaService schemaService) {
        listenerRegistration = schemaService.registerSchemaContextListener(this);
        LOG.info("DOM RPC/Action router started");
    }

    @Deprecated(forRemoval = true)
    public static DOMRpcRouter newInstance(final DOMSchemaService schemaService) {
        return new DOMRpcRouter(schemaService);
    }

    @PreDestroy
    @Deactivate
    public void shutdown() {
        close();
    }

    @Override
    public DOMActionService getActionService() {
        return actionService;
    }

    @Override
    public DOMActionProviderService getActionProviderService() {
        return actionProviderService;
    }

    @Override
    public DOMRpcService getRpcService() {
        return rpcService;
    }

    @Override
    public DOMRpcProviderService getRpcProviderService() {
        return rpcProviderService;
    }

    private synchronized void removeRpcImplementation(final DOMRpcImplementation implementation,
            final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = (DOMRpcRoutingTable) oldTable.remove(implementation, rpcs);
        routingTable = newTable;

        listenerNotifier.execute(() -> notifyRemoved(newTable, implementation));
    }

    private synchronized void removeRpcImplementations(
            final ImmutableTable<QName, YangInstanceIdentifier, DOMRpcImplementation> implTable) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = (DOMRpcRoutingTable) oldTable.removeAll(implTable);
        routingTable = newTable;

        listenerNotifier.execute(() -> notifyRemoved(newTable, implTable.values()));
    }

    private synchronized void removeActionImplementation(final DOMActionImplementation implementation,
            final Set<DOMActionInstance> actions) {
        final DOMActionRoutingTable oldTable = actionRoutingTable;
        final DOMActionRoutingTable newTable = (DOMActionRoutingTable) oldTable.remove(implementation, actions);
        actionRoutingTable = newTable;

        listenerNotifier.execute(() -> notifyActionChanged(newTable, implementation));
    }

    private synchronized void removeListener(final ListenerRegistration<? extends DOMRpcAvailabilityListener> reg) {
        listeners = ImmutableList.copyOf(Collections2.filter(listeners, input -> !reg.equals(input)));
    }

    private synchronized void removeActionListener(final ListenerRegistration<? extends AvailabilityListener> reg) {
        actionListeners = ImmutableList.copyOf(Collections2.filter(actionListeners, input -> !reg.equals(input)));
    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (Registration<?> l : listeners) {
            l.addRpc(newTable, impl);
        }
    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable,
            final Collection<? extends DOMRpcImplementation> impls) {
        for (Registration<?> l : listeners) {
            for (DOMRpcImplementation impl : impls) {
                l.addRpc(newTable, impl);
            }
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (Registration<?> l : listeners) {
            l.removeRpc(newTable, impl);
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable,
            final Collection<? extends DOMRpcImplementation> impls) {
        for (Registration<?> l : listeners) {
            for (DOMRpcImplementation impl : impls) {
                l.removeRpc(newTable, impl);
            }
        }
    }

    private synchronized void notifyActionChanged(final DOMActionRoutingTable newTable,
            final DOMActionImplementation impl) {
        for (ActionRegistration<?> l : actionListeners) {
            l.actionChanged(newTable, impl);
        }
    }

    @Override
    public synchronized void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = (DOMRpcRoutingTable) oldTable.setSchemaContext(newModelContext);
        routingTable = newTable;

        final DOMActionRoutingTable oldActionTable = actionRoutingTable;
        final DOMActionRoutingTable newActionTable =
                (DOMActionRoutingTable) oldActionTable.setSchemaContext(newModelContext);
        actionRoutingTable = newActionTable;
    }

    @Override
    protected void removeRegistration() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
        listenerNotifier.shutdown();
        LOG.info("DOM RPC/Action router stopped");
    }

    @VisibleForTesting
    synchronized List<?> listeners() {
        return listeners;
    }

    @VisibleForTesting
    synchronized List<?> actionListeners() {
        return actionListeners;
    }

    @VisibleForTesting
    DOMRpcRoutingTable routingTable() {
        return routingTable;
    }

    private static final class Registration<T extends DOMRpcAvailabilityListener>
        extends AbstractListenerRegistration<T> {

        private Map<QName, Set<YangInstanceIdentifier>> prevRpcs;
        private DOMRpcRouter router;

        Registration(final DOMRpcRouter router, final T listener, final Map<QName, Set<YangInstanceIdentifier>> rpcs) {
            super(listener);
            this.router = requireNonNull(router);
            this.prevRpcs = requireNonNull(rpcs);
        }

        @Override
        protected void removeRegistration() {
            router.removeListener(this);
            router = null;
        }

        void initialTable() {
            final List<DOMRpcIdentifier> added = new ArrayList<>();
            for (Entry<QName, Set<YangInstanceIdentifier>> e : prevRpcs.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            if (!added.isEmpty()) {
                getInstance().onRpcAvailable(added);
            }
        }

        void addRpc(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            final T l = getInstance();
            if (!l.acceptsImplementation(impl)) {
                return;
            }

            final Map<QName, Set<YangInstanceIdentifier>> rpcs = verifyNotNull(newTable.getOperations(l));
            final MapDifference<QName, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, rpcs);

            final List<DOMRpcIdentifier> added = new ArrayList<>();
            for (Entry<QName, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            for (Entry<QName, ValueDifference<Set<YangInstanceIdentifier>>> e : diff.entriesDiffering().entrySet()) {
                for (YangInstanceIdentifier i : Sets.difference(e.getValue().rightValue(), e.getValue().leftValue())) {
                    added.add(DOMRpcIdentifier.create(e.getKey(), i));
                }
            }

            prevRpcs = rpcs;
            if (!added.isEmpty()) {
                l.onRpcAvailable(added);
            }
        }

        void removeRpc(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            final T l = getInstance();
            if (!l.acceptsImplementation(impl)) {
                return;
            }

            final Map<QName, Set<YangInstanceIdentifier>> rpcs = verifyNotNull(newTable.getOperations(l));
            final MapDifference<QName, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, rpcs);

            final List<DOMRpcIdentifier> removed = new ArrayList<>();
            for (Entry<QName, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            for (Entry<QName, ValueDifference<Set<YangInstanceIdentifier>>> e : diff.entriesDiffering().entrySet()) {
                for (YangInstanceIdentifier i : Sets.difference(e.getValue().leftValue(), e.getValue().rightValue())) {
                    removed.add(DOMRpcIdentifier.create(e.getKey(), i));
                }
            }

            prevRpcs = rpcs;
            if (!removed.isEmpty()) {
                l.onRpcUnavailable(removed);
            }
        }
    }

    private static final class ActionRegistration<T extends AvailabilityListener>
        extends AbstractListenerRegistration<T> {

        private Map<Absolute, Set<DOMDataTreeIdentifier>> prevActions;
        private DOMRpcRouter router;

        ActionRegistration(final DOMRpcRouter router, final T listener,
                final Map<Absolute, Set<DOMDataTreeIdentifier>> actions) {
            super(listener);
            this.router = requireNonNull(router);
            this.prevActions = requireNonNull(actions);
        }

        @Override
        protected void removeRegistration() {
            router.removeActionListener(this);
            router = null;
        }

        void initialTable() {
            final List<DOMActionInstance> added = new ArrayList<>();
            for (Entry<Absolute, Set<DOMDataTreeIdentifier>> e : prevActions.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMActionInstance.of(e.getKey(), i)));
            }
            if (!added.isEmpty()) {
                getInstance().onActionsChanged(ImmutableSet.of(), ImmutableSet.copyOf(added));
            }
        }

        void actionChanged(final DOMActionRoutingTable newTable, final DOMActionImplementation impl) {
            final T l = getInstance();
            if (!l.acceptsImplementation(impl)) {
                return;
            }

            final Map<Absolute, Set<DOMDataTreeIdentifier>> actions = verifyNotNull(newTable.getOperations(l));
            final MapDifference<Absolute, Set<DOMDataTreeIdentifier>> diff = Maps.difference(prevActions, actions);

            final Set<DOMActionInstance> removed = new HashSet<>();
            final Set<DOMActionInstance> added = new HashSet<>();

            for (Entry<Absolute, Set<DOMDataTreeIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), i -> DOMActionInstance.of(e.getKey(), i)));
            }

            for (Entry<Absolute, Set<DOMDataTreeIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMActionInstance.of(e.getKey(), i)));
            }

            for (Entry<Absolute, ValueDifference<Set<DOMDataTreeIdentifier>>> e : diff.entriesDiffering().entrySet()) {
                for (DOMDataTreeIdentifier i : Sets.difference(e.getValue().leftValue(), e.getValue().rightValue())) {
                    removed.add(DOMActionInstance.of(e.getKey(), i));
                }

                for (DOMDataTreeIdentifier i : Sets.difference(e.getValue().rightValue(), e.getValue().leftValue())) {
                    added.add(DOMActionInstance.of(e.getKey(), i));
                }
            }

            prevActions = actions;
            if (!removed.isEmpty() || !added.isEmpty()) {
                l.onActionsChanged(removed, added);
            }
        }
    }

    @NonNullByDefault
    private final class ActionAvailabilityFacade implements DOMActionAvailabilityExtension {
        @Override
        public <T extends AvailabilityListener> ListenerRegistration<T> registerAvailabilityListener(final T listener) {
            synchronized (DOMRpcRouter.this) {
                final ActionRegistration<T> ret = new ActionRegistration<>(DOMRpcRouter.this, listener,
                    actionRoutingTable.getOperations(listener));
                actionListeners = ImmutableList.<ActionRegistration<?>>builder()
                    .addAll(actionListeners)
                    .add(ret)
                    .build();

                listenerNotifier.execute(ret::initialTable);
                return ret;
            }
        }
    }

    @NonNullByDefault
    private final class ActionServiceFacade implements DOMActionService {
        private final ClassToInstanceMap<DOMActionServiceExtension> extensions = ImmutableClassToInstanceMap.of(
            DOMActionAvailabilityExtension.class, new ActionAvailabilityFacade());

        @Override
        public ClassToInstanceMap<DOMActionServiceExtension> getExtensions() {
            return extensions;
        }

        @Override
        public ListenableFuture<? extends DOMActionResult> invokeAction(final Absolute type,
                final DOMDataTreeIdentifier path, final ContainerNode input) {
            final YangInstanceIdentifier pathRoot = path.getRootIdentifier();
            checkArgument(!pathRoot.isEmpty(), "Action path must not be empty");

            final DOMActionRoutingTableEntry entry = (DOMActionRoutingTableEntry) actionRoutingTable.getEntry(type);
            return entry != null ? OperationInvocation.invoke(entry, type, path, requireNonNull(input))
                : Futures.immediateFailedFuture(
                    new DOMActionNotAvailableException("No implementation of Action %s available", type));
        }
    }

    @NonNullByDefault
    private final class ActionProviderServiceFacade implements DOMActionProviderService {
        @Override
        public <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
                final T implementation, final Set<DOMActionInstance> instances) {

            synchronized (DOMRpcRouter.this) {
                final DOMActionRoutingTable oldTable = actionRoutingTable;
                final DOMActionRoutingTable newTable = (DOMActionRoutingTable) oldTable.add(implementation, instances);
                actionRoutingTable = newTable;

                listenerNotifier.execute(() -> notifyActionChanged(newTable, implementation));
            }

            return new AbstractObjectRegistration<>(implementation) {
                @Override
                protected void removeRegistration() {
                    removeActionImplementation(getInstance(), instances);
                }
            };
        }
    }

    private final class RpcServiceFacade implements DOMRpcService {
        @Override
        public ListenableFuture<? extends DOMRpcResult> invokeRpc(final QName type, final NormalizedNode input) {
            final AbstractDOMRpcRoutingTableEntry entry = (AbstractDOMRpcRoutingTableEntry) routingTable.getEntry(type);
            if (entry == null) {
                return Futures.immediateFailedFuture(
                    new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", type));
            }

            return OperationInvocation.invoke(entry, requireNonNull(input));
        }

        @Override
        public <T extends DOMRpcAvailabilityListener> ListenerRegistration<T> registerRpcListener(final T listener) {
            synchronized (DOMRpcRouter.this) {
                final Registration<T> ret = new Registration<>(DOMRpcRouter.this, listener,
                    routingTable.getOperations(listener));
                listeners = ImmutableList.<Registration<?>>builder().addAll(listeners).add(ret).build();

                listenerNotifier.execute(ret::initialTable);
                return ret;
            }
        }
    }

    private final class RpcProviderServiceFacade implements DOMRpcProviderService {
        @Override
        public <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T> registerRpcImplementation(
                final T implementation, final DOMRpcIdentifier... rpcs) {
            return registerRpcImplementation(implementation, ImmutableSet.copyOf(rpcs));
        }

        @Override
        public <T extends DOMRpcImplementation> DOMRpcImplementationRegistration<T> registerRpcImplementation(
                final T implementation, final Set<DOMRpcIdentifier> rpcs) {

            synchronized (DOMRpcRouter.this) {
                final DOMRpcRoutingTable oldTable = routingTable;
                final DOMRpcRoutingTable newTable = (DOMRpcRoutingTable) oldTable.add(implementation, rpcs);
                routingTable = newTable;

                listenerNotifier.execute(() -> notifyAdded(newTable, implementation));
            }

            return new AbstractDOMRpcImplementationRegistration<>(implementation) {
                @Override
                protected void removeRegistration() {
                    removeRpcImplementation(getInstance(), rpcs);
                }
            };
        }

        @Override
        public org.opendaylight.yangtools.concepts.Registration registerRpcImplementations(
                final Map<DOMRpcIdentifier, DOMRpcImplementation> map) {
            checkArgument(!map.isEmpty());

            final var builder = ImmutableTable.<QName, YangInstanceIdentifier, DOMRpcImplementation>builder();
            for (var entry : map.entrySet()) {
                final var id = entry.getKey();
                builder.put(id.getType(), id.getContextReference(), entry.getValue());
            }
            final var implTable = builder.build();

            synchronized (DOMRpcRouter.this) {
                final DOMRpcRoutingTable oldTable = routingTable;
                final DOMRpcRoutingTable newTable = (DOMRpcRoutingTable) oldTable.addAll(implTable);
                routingTable = newTable;

                listenerNotifier.execute(() -> notifyAdded(newTable, implTable.values()));
            }

            return new AbstractRegistration() {
                @Override
                protected void removeRegistration() {
                    removeRpcImplementations(implTable);
                }
            };
        }
    }

    static final class OperationInvocation {
        private static final Logger LOG = LoggerFactory.getLogger(OperationInvocation.class);

        static ListenableFuture<? extends DOMActionResult> invoke(final DOMActionRoutingTableEntry entry,
                final Absolute type, final DOMDataTreeIdentifier path, final ContainerNode input) {
            List<DOMActionImplementation> impls = entry.getImplementations(path);
            if (impls == null) {
                impls = entry.getImplementations(
                    new DOMDataTreeIdentifier(path.getDatastoreType(), YangInstanceIdentifier.empty()));
                if (impls == null) {
                    return Futures.immediateFailedFuture(new DOMActionNotAvailableException(
                        "No implementation of Action %s available for %s", type, path));
                }
            }

            return impls.get(0).invokeAction(type, path, input);
        }

        static ListenableFuture<? extends DOMRpcResult> invoke(final AbstractDOMRpcRoutingTableEntry entry,
                final NormalizedNode input) {
            if (entry instanceof UnknownDOMRpcRoutingTableEntry) {
                return Futures.immediateFailedFuture(
                    new DOMRpcImplementationNotAvailableException("%s is not resolved to an RPC", entry.getType()));
            } else if (entry instanceof RoutedDOMRpcRoutingTableEntry) {
                return invokeRoutedRpc((RoutedDOMRpcRoutingTableEntry) entry, input);
            } else if (entry instanceof GlobalDOMRpcRoutingTableEntry) {
                return invokeGlobalRpc((GlobalDOMRpcRoutingTableEntry) entry, input);
            }

            return Futures.immediateFailedFuture(
                new DOMRpcImplementationNotAvailableException("Unsupported RPC entry."));
        }

        private static ListenableFuture<? extends DOMRpcResult> invokeRoutedRpc(
                final RoutedDOMRpcRoutingTableEntry entry, final NormalizedNode input) {
            final Optional<NormalizedNode> maybeKey = NormalizedNodes.findNode(input,
                entry.getRpcId().getContextReference());

            // Routing key is present, attempt to deliver as a routed RPC
            if (maybeKey.isPresent()) {
                final NormalizedNode key = maybeKey.get();
                final Object value = key.body();
                if (value instanceof YangInstanceIdentifier) {
                    final YangInstanceIdentifier iid = (YangInstanceIdentifier) value;

                    // Find a DOMRpcImplementation for a specific iid
                    final List<DOMRpcImplementation> specificImpls = entry.getImplementations(iid);
                    if (specificImpls != null) {
                        return specificImpls.get(0)
                            .invokeRpc(DOMRpcIdentifier.create(entry.getType(), iid), input);
                    }

                    LOG.debug("No implementation for context {} found will now look for wildcard id", iid);

                    // Find a DOMRpcImplementation for a wild card. Usually remote-rpc-connector would register an
                    // implementation this way
                    final List<DOMRpcImplementation> mayBeRemoteImpls =
                        entry.getImplementations(YangInstanceIdentifier.empty());

                    if (mayBeRemoteImpls != null) {
                        return mayBeRemoteImpls.get(0)
                            .invokeRpc(DOMRpcIdentifier.create(entry.getType(), iid), input);
                    }

                } else {
                    LOG.warn("Ignoring wrong context value {}", value);
                }
            }

            final List<DOMRpcImplementation> impls = entry.getImplementations(null);
            if (impls != null) {
                return impls.get(0).invokeRpc(entry.getRpcId(), input);
            }

            return Futures.immediateFailedFuture(
                new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available",
                    entry.getType()));
        }

        private static ListenableFuture<? extends DOMRpcResult> invokeGlobalRpc(
                final GlobalDOMRpcRoutingTableEntry entry, final NormalizedNode input) {
            return entry.getImplementations(YangInstanceIdentifier.empty()).get(0).invokeRpc(entry.getRpcId(), input);
        }
    }
}
