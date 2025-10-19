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
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = DOMRpcRouter.class)
public final class DOMRpcRouter extends AbstractRegistration {
    @NonNullByDefault
    private final class ActionReg extends AbstractRegistration {
        private final DOMActionImplementation implementation;
        private final ImmutableSet<DOMActionInstance> instances;

        ActionReg(final DOMActionImplementation implementation, final Set<DOMActionInstance> instances) {
            this.implementation = requireNonNull(implementation);
            this.instances = ImmutableSet.copyOf(instances);
        }

        @Override
        protected void removeRegistration() {
            synchronized (DOMRpcRouter.this) {
                final var oldTable = actionRoutingTable;
                final var newTable = (DOMActionRoutingTable) oldTable.remove(implementation, instances);
                actionRoutingTable = newTable;

                listenerNotifier.execute(() -> notifyActionChanged(newTable, implementation));
            }
        }

        @Override
        protected ToStringHelper addToStringAttributes(ToStringHelper helper) {
            return super.addToStringAttributes(helper
                .add("implementation", implementation)
                .add("instances", instances));
        }
    }

    private static final class ActionAvailReg extends AbstractRegistration {
        private final @NonNull AvailabilityListener listener;

        private Map<Absolute, Set<DOMDataTreeIdentifier>> prevActions;
        private DOMRpcRouter router;

        ActionAvailReg(final DOMRpcRouter router, final AvailabilityListener listener,
                final Map<Absolute, Set<DOMDataTreeIdentifier>> actions) {
            this.listener = requireNonNull(listener);
            this.router = requireNonNull(router);
            prevActions = requireNonNull(actions);
        }

        @Override
        protected void removeRegistration() {
            router.removeActionListener(this);
            router = null;
        }

        void initialTable() {
            final var added = new ArrayList<DOMActionInstance>();
            for (var e : prevActions.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMActionInstance.of(e.getKey(), i)));
            }
            if (!added.isEmpty()) {
                listener.onActionsChanged(ImmutableSet.of(), ImmutableSet.copyOf(added));
            }
        }

        void actionChanged(final DOMActionRoutingTable newTable, final DOMActionImplementation impl) {
            if (!listener.acceptsImplementation(impl)) {
                return;
            }

            final var actions = verifyNotNull(newTable.getOperations(listener));
            final var diff = Maps.difference(prevActions, actions);

            final var removed = new HashSet<DOMActionInstance>();
            final var added = new HashSet<DOMActionInstance>();

            for (var le : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(le.getValue(), i -> DOMActionInstance.of(le.getKey(), i)));
            }

            for (var re : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(re.getValue(), i -> DOMActionInstance.of(re.getKey(), i)));
            }

            for (var entry : diff.entriesDiffering().entrySet()) {
                for (var dti : Sets.difference(entry.getValue().leftValue(), entry.getValue().rightValue())) {
                    removed.add(DOMActionInstance.of(entry.getKey(), dti));
                }

                for (var dti : Sets.difference(entry.getValue().rightValue(), entry.getValue().leftValue())) {
                    added.add(DOMActionInstance.of(entry.getKey(), dti));
                }
            }

            prevActions = actions;
            if (!removed.isEmpty() || !added.isEmpty()) {
                listener.onActionsChanged(removed, added);
            }
        }

        @Override
        protected ToStringHelper addToStringAttributes(ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("listener", listener));
        }
    }

    @NonNullByDefault
    private final class RpcReg extends AbstractRegistration {
        private final DOMRpcImplementation implementation;
        private final ImmutableSet<DOMRpcIdentifier> instances;

        RpcReg(final DOMRpcImplementation implementation, final Set<DOMRpcIdentifier> instances) {
            this.implementation = requireNonNull(implementation);
            this.instances = ImmutableSet.copyOf(instances);
        }

        @Override
        protected void removeRegistration() {
            synchronized (DOMRpcRouter.this) {
                final var oldTable = routingTable;
                final var newTable = (DOMRpcRoutingTable) oldTable.remove(implementation, instances);
                routingTable = newTable;

                listenerNotifier.execute(() -> notifyRemoved(newTable, implementation));
            }
        }

        @Override
        protected ToStringHelper addToStringAttributes(ToStringHelper helper) {
            return super.addToStringAttributes(helper
                .add("implementation", implementation)
                .add("instances", instances));
        }
    }

    @NonNullByDefault
    private final class RpcsReg extends AbstractRegistration {
        private final ImmutableTable<QName, YangInstanceIdentifier, DOMRpcImplementation> table;

        RpcsReg(final Map<DOMRpcIdentifier, DOMRpcImplementation> map) {
            final var builder = ImmutableTable.<QName, YangInstanceIdentifier, DOMRpcImplementation>builder();
            for (var entry : map.entrySet()) {
                final var id = entry.getKey();
                builder.put(id.getType(), id.getContextReference(), entry.getValue());
            }
            table = builder.build();
        }

        @Override
        protected void removeRegistration() {
            synchronized (DOMRpcRouter.this) {
                final var oldTable = routingTable;
                final var newTable = (DOMRpcRoutingTable) oldTable.removeAll(table);
                routingTable = newTable;

                listenerNotifier.execute(() -> notifyRemoved(newTable, table.values()));
            }
        }

        @Override
        protected ToStringHelper addToStringAttributes(ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("table", table));
        }
    }

    private static final class RpcAvailReg extends AbstractRegistration {
        private final @NonNull DOMRpcAvailabilityListener listener;

        private Map<QName, Set<YangInstanceIdentifier>> prevRpcs;
        private DOMRpcRouter router;

        RpcAvailReg(final DOMRpcRouter router, final DOMRpcAvailabilityListener listener,
                final Map<QName, Set<YangInstanceIdentifier>> rpcs) {
            this.listener = requireNonNull(listener);
            this.router = requireNonNull(router);
            prevRpcs = requireNonNull(rpcs);
        }

        @Override
        protected void removeRegistration() {
            router.removeListener(this);
            router = null;
        }

        void initialTable() {
            final var added = new ArrayList<DOMRpcIdentifier>();
            for (var e : prevRpcs.entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            if (!added.isEmpty()) {
                listener.onRpcAvailable(added);
            }
        }

        void addRpc(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            if (!listener.acceptsImplementation(impl)) {
                return;
            }

            final var rpcs = verifyNotNull(newTable.getOperations(listener));
            final var diff = Maps.difference(prevRpcs, rpcs);

            final var added = new ArrayList<DOMRpcIdentifier>();
            for (var e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), ref -> DOMRpcIdentifier.create(e.getKey(), ref)));
            }
            for (var e : diff.entriesDiffering().entrySet()) {
                for (var ref : Sets.difference(e.getValue().rightValue(), e.getValue().leftValue())) {
                    added.add(DOMRpcIdentifier.create(e.getKey(), ref));
                }
            }

            prevRpcs = rpcs;
            if (!added.isEmpty()) {
                listener.onRpcAvailable(added);
            }
        }

        void removeRpc(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
            if (!listener.acceptsImplementation(impl)) {
                return;
            }

            final var rpcs = verifyNotNull(newTable.getOperations(listener));
            final var diff = Maps.difference(prevRpcs, rpcs);

            final var removed = new ArrayList<DOMRpcIdentifier>();
            for (var e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), ref -> DOMRpcIdentifier.create(e.getKey(), ref)));
            }
            for (var e : diff.entriesDiffering().entrySet()) {
                for (var ref : Sets.difference(e.getValue().leftValue(), e.getValue().rightValue())) {
                    removed.add(DOMRpcIdentifier.create(e.getKey(), ref));
                }
            }

            prevRpcs = rpcs;
            if (!removed.isEmpty()) {
                listener.onRpcUnavailable(removed);
            }
        }

        @Override
        protected ToStringHelper addToStringAttributes(ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("listener", listener));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMRpcRouter.class);
    private static final ThreadFactory THREAD_FACTORY = Thread.ofPlatform().daemon().name("DOMRpcRouter-listener-", 0)
        .factory();

    private final ExecutorService listenerNotifier = Executors.newSingleThreadExecutor(THREAD_FACTORY);
    private final @NonNull DOMActionProviderService actionProviderService = new RouterDOMActionProviderService(this);
    private final @NonNull DOMActionService actionService = new RouterDOMActionService(this);
    private final @NonNull DOMRpcProviderService rpcProviderService = new RouterDOMRpcProviderService(this);
    private final @NonNull DOMRpcService rpcService = new RouterDOMRpcService(this);

    private @GuardedBy("this") ImmutableList<RpcAvailReg> listeners = ImmutableList.of();
    private @GuardedBy("this") ImmutableList<ActionAvailReg> actionListeners = ImmutableList.of();
    private volatile DOMRpcRoutingTable routingTable = DOMRpcRoutingTable.EMPTY;
    private volatile DOMActionRoutingTable actionRoutingTable = DOMActionRoutingTable.EMPTY;
    private Registration listenerRegistration;

    @Deprecated
    @VisibleForTesting
    // FIXME: 9.0.0: make this constructor package-private
    public DOMRpcRouter() {

    }

    @Inject
    @Activate
    public DOMRpcRouter(@Reference final DOMSchemaService schemaService) {
        listenerRegistration = schemaService.registerSchemaContextListener(this::onModelContextUpdated);
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

    @Deprecated(since = "14.0.15", forRemoval = true)
    public @NonNull DOMActionService actionService() {
        return actionService;
    }

    @NonNullByDefault
    ListenableFuture<? extends DOMRpcResult> invokeAction(final Absolute type, final DOMDataTreeIdentifier path,
            final ContainerNode input) {
        checkArgument(!path.path().isEmpty(), "Action path must not be empty");

        final var entry = (DOMActionRoutingTableEntry) actionRoutingTable.getEntry(type);
        return entry != null ? OperationInvocation.invoke(entry, type, path, requireNonNull(input))
            : Futures.immediateFailedFuture(
                new DOMActionNotAvailableException("No implementation of Action %s available", type));
    }

    @NonNullByDefault
    synchronized Registration registerAvailabilityListener(final AvailabilityListener listener) {
        final var ret = new ActionAvailReg(this, listener, actionRoutingTable.getOperations(listener));
        actionListeners = ImmutableList.<ActionAvailReg>builder()
            .addAll(actionListeners)
            .add(ret)
            .build();

        listenerNotifier.execute(ret::initialTable);
        return ret;
    }

    @Deprecated(since = "14.0.15", forRemoval = true)
    public @NonNull DOMActionProviderService actionProviderService() {
        return actionProviderService;
    }

    @NonNullByDefault
    Registration registerActionImplementation(final DOMActionImplementation implementation,
             final Set<DOMActionInstance> instances) {
        final var reg = new ActionReg(implementation, instances);
        if (reg.instances.isEmpty()) {
            throw new IllegalArgumentException("Instances must not be empty");
        }

        synchronized (this) {
            final var oldTable = actionRoutingTable;
            final var newTable = (DOMActionRoutingTable) oldTable.add(implementation, reg.instances);
            actionRoutingTable = newTable;

            listenerNotifier.execute(() -> notifyActionChanged(newTable, implementation));
        }

        return reg;
    }

    @Deprecated(since = "14.0.15", forRemoval = true)
    public @NonNull DOMRpcService rpcService() {
        return rpcService;
    }

    @NonNullByDefault
    ListenableFuture<? extends DOMRpcResult> invokeRpc(final QName type, final ContainerNode input) {
        final var entry = (DOMRpcRoutingTableEntry) routingTable.getEntry(type);
        return entry != null ? OperationInvocation.invoke(entry, requireNonNull(input))
            : Futures.immediateFailedFuture(
                new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", type));
    }

    @NonNullByDefault
    synchronized Registration registerRpcListener(final DOMRpcAvailabilityListener listener) {
        final var ret = new RpcAvailReg(this, listener, routingTable.getOperations(listener));
        listeners = ImmutableList.<RpcAvailReg>builder().addAll(listeners).add(ret).build();

        listenerNotifier.execute(ret::initialTable);
        return ret;
    }

    @Deprecated(since = "14.0.15", forRemoval = true)
    public @NonNull DOMRpcProviderService rpcProviderService() {
        return rpcProviderService;
    }

    @NonNullByDefault
    Registration registerRpcImplementation(final DOMRpcImplementation implementation,
            final Set<DOMRpcIdentifier> rpcs) {
        final var reg = new RpcReg(implementation, rpcs);

        synchronized (this) {
            final var oldTable = routingTable;
            final var newTable = (DOMRpcRoutingTable) oldTable.add(implementation, reg.instances);
            routingTable = newTable;

            listenerNotifier.execute(() -> notifyAdded(newTable, implementation));
        }

        return reg;
    }

    @NonNullByDefault
    Registration registerRpcImplementations(final Map<DOMRpcIdentifier, DOMRpcImplementation> map) {
        final var reg = new RpcsReg(map);
        if (reg.table.isEmpty()) {
            throw new IllegalArgumentException("Implementation map must not be empty");
        }

        synchronized (this) {
            final var oldTable = routingTable;
            final var newTable = (DOMRpcRoutingTable) oldTable.addAll(reg.table);
            routingTable = newTable;

            listenerNotifier.execute(() -> notifyAdded(newTable, reg.table.values()));
        }

        return reg;
    }

    private synchronized void removeListener(final RpcAvailReg reg) {
        listeners = listeners.stream()
            .filter(input -> !reg.equals(input))
            .collect(ImmutableList.toImmutableList());
    }

    private synchronized void removeActionListener(final ActionAvailReg reg) {
        actionListeners = actionListeners.stream()
            .filter(input -> !reg.equals(input))
            .collect(ImmutableList.toImmutableList());
    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (var l : listeners) {
            l.addRpc(newTable, impl);
        }
    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable,
            final Collection<? extends DOMRpcImplementation> impls) {
        for (var l : listeners) {
            for (var impl : impls) {
                l.addRpc(newTable, impl);
            }
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (var l : listeners) {
            l.removeRpc(newTable, impl);
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable,
            final Collection<? extends DOMRpcImplementation> impls) {
        for (var l : listeners) {
            for (DOMRpcImplementation impl : impls) {
                l.removeRpc(newTable, impl);
            }
        }
    }

    private synchronized void notifyActionChanged(final DOMActionRoutingTable newTable,
            final DOMActionImplementation impl) {
        for (var l : actionListeners) {
            l.actionChanged(newTable, impl);
        }
    }

    synchronized void onModelContextUpdated(final @NonNull EffectiveModelContext newModelContext) {
        final var oldTable = routingTable;
        final var newTable = (DOMRpcRoutingTable) oldTable.setSchemaContext(newModelContext);
        routingTable = newTable;

        final var oldActionTable = actionRoutingTable;
        final var newActionTable = (DOMActionRoutingTable) oldActionTable.setSchemaContext(newModelContext);
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
}
