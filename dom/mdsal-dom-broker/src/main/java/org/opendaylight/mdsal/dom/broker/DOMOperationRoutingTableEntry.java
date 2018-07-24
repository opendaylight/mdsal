/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FluentFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Definitions of operation routing tables.
 *
 * @param <T> Type of operation,  {@link QName} for RPC or {@link SchemaPath} for Action
 * @param <D> {@link YangInstanceIdentifier} for RPC or {@link DOMDataTreeIdentifier} for Action
 * @param <E> {@link DOMRpcRoutingTableEntry} or {@link DOMActionRoutingTableEntry}
 * @param <M> {@link DOMOperationImplementation.Rpc} or {@link DOMOperationImplementation.Action}
 */
@Beta
abstract class DOMOperationRoutingTableEntry<T, D, E extends DOMOperationRoutingTableEntry<T, D, E, M>,
        M extends DOMOperationImplementation> {
    private final Map<D, List<M>> implementations;
    private final T type;

    private DOMOperationRoutingTableEntry(final T type, final Map<D, List<M>> implementations) {
        this.type = Preconditions.checkNotNull(type);
        this.implementations = Preconditions.checkNotNull(implementations);
    }

    private DOMOperationRoutingTableEntry() {
        this.type = null;
        this.implementations = null;
    }

    final T getType() {
        return type;
    }

    final List<M> getImplementations(final D identifier) {
        return implementations.get(identifier);
    }

    final Map<D, List<M>> getImplementations() {
        return implementations;
    }

    final Set<D> registeredIdentifiers(final AvailabilityListener listener) {
        return Maps.filterValues(implementations, list -> list.stream()
            .anyMatch(listener::acceptsImplementation)).keySet();
    }

    @VisibleForTesting
    final Set<D> registeredIdentifiers() {
        return implementations.keySet();
    }

    /**
     * This method adds the given DOMOperationImplementation instance for the given list operation identifiers.
     *
     * @param implementation the DOMOperationImplementation instance to add
     * @param newOperations  the List of new RPCs/Actions that the DOMOperationImplementation provides, must be mutable
     * @return a new instance of DOMOperationRoutingTableEntry with the additions
     */
    final E add(final M implementation, final List<D> newOperations) {
        final Builder<D, List<M>> vb = ImmutableMap.builder();
        for (final Entry<D, List<M>> ve : implementations.entrySet()) {
            if (newOperations.remove(ve.getKey())) {
                final List<M> i = new ArrayList<>(ve.getValue().size() + 1);
                i.addAll(ve.getValue());
                i.add(implementation);

                // New implementation is at the end, this will move it to be the last among implementations
                // with equal cost -- relying on sort() being stable.
                i.sort((i1, i2) -> Long.compare(i1.invocationCost(), i2.invocationCost()));
                vb.put(ve.getKey(), i);
            } else {
                vb.put(ve);
            }
        }
        for (final D ii : newOperations) {
            final List<M> impl = new ArrayList<>(1);
            impl.add(implementation);
            vb.put(ii, impl);
        }

        return newInstance(vb.build());
    }

    final E remove(final M implementation, final List<D> removed) {
        final Builder<D, List<M>> vb = ImmutableMap.builder();
        for (final Entry<D, List<M>> ve : implementations.entrySet()) {
            if (removed.remove(ve.getKey())) {
                final List<M> i = new ArrayList<>(ve.getValue());
                i.remove(implementation);
                // We could trimToSize(), but that may perform another copy just to get rid
                // of a single element. That is probably not worth the trouble.
                if (!i.isEmpty()) {
                    vb.put(ve.getKey(), i);
                }
            } else {
                vb.put(ve);
            }
        }

        final Map<D, List<M>> v = vb.build();
        return v.isEmpty() ? null : newInstance(vb.build());
    }

    protected abstract E newInstance(Map<D, List<M>> impls);

    public static final class DOMRpcRoutingTableEntry extends DOMOperationRoutingTableEntry<QName,
            YangInstanceIdentifier, DOMRpcRoutingTableEntry, DOMOperationImplementation.Rpc> {

        private DOMRpcRoutingTableEntry(QName type, Map<YangInstanceIdentifier,
                List<DOMOperationImplementation.Rpc>> implementations) {
            super(type, implementations);
        }

        public FluentFuture<? extends DOMOperationResult> invokeRpc(ContainerNode input) {
            return getImplementations(YangInstanceIdentifier.EMPTY).get(0).invokeRpc(getType(), input);
        }

        @Override
        protected DOMRpcRoutingTableEntry newInstance(
                Map<YangInstanceIdentifier, List<DOMOperationImplementation.Rpc>> impls) {
            return new DOMRpcRoutingTableEntry(getType(), impls);
        }

    }

    static final class DOMActionRoutingTableEntry extends DOMOperationRoutingTableEntry<SchemaPath,
            DOMDataTreeIdentifier, DOMActionRoutingTableEntry, DOMOperationImplementation.Action> {

        private DOMActionRoutingTableEntry(SchemaPath type,
                Map<DOMDataTreeIdentifier, List<DOMOperationImplementation.Action>> implementations) {
            super(type, implementations);
        }

        public FluentFuture<? extends DOMOperationResult> invokeAction(DOMDataTreeIdentifier id, ContainerNode input) {
            //FIXME:
            return getImplementations(id).get(0).invokeAction(getType(), id, input);
        }

        @Override
        protected DOMActionRoutingTableEntry newInstance(
                Map<DOMDataTreeIdentifier, List<DOMOperationImplementation.Action>> impls) {
            return new DOMActionRoutingTableEntry(getType(), impls);
        }
    }

    static DOMRpcRoutingTableEntry rpcEntry(QName type,
        Map<YangInstanceIdentifier, List<DOMOperationImplementation.Rpc>> implementations) {
        return new DOMRpcRoutingTableEntry(type, implementations);
    }

    static DOMActionRoutingTableEntry actionEntry(SchemaPath type,
            Map<DOMDataTreeIdentifier, List<DOMOperationImplementation.Action>> implementations) {
        return new DOMActionRoutingTableEntry(type, implementations);
    }
}
