/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.concurrent.GuardedBy;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityExtension;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation.Action;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation.Rpc;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderService;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderServiceExtension;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMOperationServiceExtension;
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
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class DOMRpcRouter extends AbstractRegistration implements SchemaContextListener {
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(
            "DOMRpcRouter-listener-%s").setDaemon(true).build();

    private final ExecutorService listenerNotifier = Executors.newSingleThreadExecutor(THREAD_FACTORY);
    private final DOMOperationProviderService operationProviderService = new OperationProviderServiceFacade();
    private final DOMOperationService operationService = new OperationServiceFacade();
    private final DOMRpcProviderService rpcProviderService = new RpcProviderServiceFacade();
    private final DOMRpcService rpcService = new RpcServiceFacade();

    @GuardedBy("this")
    private Collection<Registration<?>> listeners = Collections.emptyList();

    private volatile DOMRpcRoutingTable routingTable = DOMRpcRoutingTable.EMPTY;

    private ListenerRegistration<?> listenerRegistration;

    public static DOMRpcRouter newInstance(final DOMSchemaService schemaService) {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        rpcRouter.listenerRegistration = schemaService.registerSchemaContextListener(rpcRouter);
        return rpcRouter;
    }

    public DOMOperationService getOperationService() {
        return operationService;
    }

    public DOMOperationProviderService getOperationProviderService() {
        return operationProviderService;
    }

    public DOMRpcService getRpcService() {
        return rpcService;
    }

    public DOMRpcProviderService getRpcProviderService() {
        return rpcProviderService;
    }

    private synchronized void removeRpcImplementation(final DOMRpcImplementation implementation,
            final Set<DOMRpcIdentifier> rpcs) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.remove(implementation, rpcs);
        routingTable = newTable;

        listenerNotifier.execute(() -> notifyRemoved(newTable, implementation));
    }

    private synchronized void removeListener(final ListenerRegistration<? extends DOMRpcAvailabilityListener> reg) {
        listeners = ImmutableList.copyOf(Collections2.filter(listeners, input -> !reg.equals(input)));
    }

    private synchronized void notifyAdded(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (Registration<?> l : listeners) {
            l.addRpc(newTable, impl);
        }
    }

    private synchronized void notifyRemoved(final DOMRpcRoutingTable newTable, final DOMRpcImplementation impl) {
        for (Registration<?> l : listeners) {
            l.removeRpc(newTable, impl);
        }
    }

    @Override
    public synchronized void onGlobalContextUpdated(final SchemaContext context) {
        final DOMRpcRoutingTable oldTable = routingTable;
        final DOMRpcRoutingTable newTable = oldTable.setSchemaContext(context);
        routingTable = newTable;
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
    DOMRpcRoutingTable routingTable() {
        return routingTable;
    }

    private static final class Registration<T extends DOMRpcAvailabilityListener>
        extends AbstractListenerRegistration<T> {

        private Map<SchemaPath, Set<YangInstanceIdentifier>> prevRpcs;
        private DOMRpcRouter router;

        Registration(final DOMRpcRouter router, final T listener,
                final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs) {
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
            final Collection<DOMRpcIdentifier> added = new ArrayList<>();
            for (Entry<SchemaPath, Set<YangInstanceIdentifier>> e : prevRpcs.entrySet()) {
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

            final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs = verifyNotNull(newTable.getRpcs(l));
            final MapDifference<SchemaPath, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, rpcs);

            final Collection<DOMRpcIdentifier> added = new ArrayList<>();
            for (Entry<SchemaPath, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnRight().entrySet()) {
                added.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            for (Entry<SchemaPath, ValueDifference<Set<YangInstanceIdentifier>>> e :
                    diff.entriesDiffering().entrySet()) {
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

            final Map<SchemaPath, Set<YangInstanceIdentifier>> rpcs = verifyNotNull(newTable.getRpcs(l));
            final MapDifference<SchemaPath, Set<YangInstanceIdentifier>> diff = Maps.difference(prevRpcs, rpcs);

            final Collection<DOMRpcIdentifier> removed = new ArrayList<>();
            for (Entry<SchemaPath, Set<YangInstanceIdentifier>> e : diff.entriesOnlyOnLeft().entrySet()) {
                removed.addAll(Collections2.transform(e.getValue(), i -> DOMRpcIdentifier.create(e.getKey(), i)));
            }
            for (Entry<SchemaPath, ValueDifference<Set<YangInstanceIdentifier>>> e :
                    diff.entriesDiffering().entrySet()) {
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

    @NonNullByDefault
    private final class OperationAvailabilityFacade implements DOMOperationAvailabilityExtension {
        @Override
        public <T extends AvailabilityListener> ListenerRegistration<T> registerAvailabilityListener(final T listener) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @NonNullByDefault
    private final class OperationServiceFacade implements DOMOperationService {
        private final ClassToInstanceMap<DOMOperationServiceExtension> extensions = ImmutableClassToInstanceMap.of(
            DOMOperationAvailabilityExtension.class, new OperationAvailabilityFacade());

        @Override
        public ClassToInstanceMap<DOMOperationServiceExtension> getExtensions() {
            return extensions;
        }

        @Override
        public FluentFuture<DOMOperationResult> invokeRpc(final QName type, final ContainerNode input) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FluentFuture<DOMOperationResult> invokeAction(final SchemaPath type, final DOMDataTreeIdentifier path,
                final ContainerNode input) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @NonNullByDefault
    private final class OperationProviderServiceFacade implements DOMOperationProviderService {
        @Override
        public ClassToInstanceMap<DOMOperationProviderServiceExtension> getExtensions() {
            return ImmutableClassToInstanceMap.of();
        }

        @Override
        public <T extends Action> ObjectRegistration<T> registerActionImplementation(
                final T implementation, final Set<DOMOperationInstance.Action> instances) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends Rpc> ObjectRegistration<T> registerRpcImplementation(final T implementation,
                final Set<DOMOperationInstance.Rpc> instances) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private final class RpcServiceFacade implements DOMRpcService {
        @Override
        public FluentFuture<DOMRpcResult> invokeRpc(final SchemaPath type, final NormalizedNode<?, ?> input) {
            final AbstractDOMRpcRoutingTableEntry entry = routingTable.getEntry(type);
            if (entry == null) {
                return FluentFutures.immediateFailedFluentFuture(
                    new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", type));
            }

            return entry.invokeRpc(input);
        }

        @Override
        public <T extends DOMRpcAvailabilityListener> ListenerRegistration<T> registerRpcListener(final T listener) {
            synchronized (DOMRpcRouter.this) {
                final Registration<T> ret = new Registration<>(DOMRpcRouter.this, listener,
                        routingTable.getRpcs(listener));
                final Builder<Registration<?>> b = ImmutableList.builder();
                b.addAll(listeners);
                b.add(ret);
                listeners = b.build();

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
                final DOMRpcRoutingTable newTable = oldTable.add(implementation, rpcs);
                routingTable = newTable;

                listenerNotifier.execute(() -> notifyAdded(newTable, implementation));
            }

            return new AbstractDOMRpcImplementationRegistration<T>(implementation) {
                @Override
                protected void removeRegistration() {
                    removeRpcImplementation(getInstance(), rpcs);
                }
            };
        }
    }
}
