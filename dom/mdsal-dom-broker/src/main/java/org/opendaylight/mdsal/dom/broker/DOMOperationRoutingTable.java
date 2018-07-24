/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.actionEntry;
import static org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.rpcEntry;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance.Action;
import org.opendaylight.mdsal.dom.api.DOMOperationInstance.Rpc;
import org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.DOMActionRoutingTableEntry;
import org.opendaylight.mdsal.dom.broker.DOMOperationRoutingTableEntry.DOMRpcRoutingTableEntry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;


/**
 * Definitions of operation routing tables.
 *
 * @param <T> Type of operation,  {@link QName} for RPC or {@link SchemaPath} for Action
 * @param <D> {@link YangInstanceIdentifier} for RPC or {@link DOMDataTreeIdentifier} for Action
 * @param <M> {@link DOMOperationImplementation.Rpc} or {@link DOMOperationImplementation.Action}
 * @param <E> {@link DOMRpcRoutingTableEntry} or {@link DOMActionRoutingTableEntry}
 * @param <I> {@link DOMOperationInstance.Rpc} or {@link DOMOperationInstance.Action}
 * @param <R> {@link DOMOperationRoutingTable.DOMRpcRoutingTable} or
 *            {@link DOMOperationRoutingTable.DOMActionRoutingTable}
 */
@Beta
@NonNullByDefault
public abstract class DOMOperationRoutingTable<T, D , M extends DOMOperationImplementation,
        E extends DOMOperationRoutingTableEntry<T, D, E ,M>, I extends DOMOperationInstance<T>,
        R extends DOMOperationRoutingTable<T, D, M, E, I, R>> {

    private final Map<T, E> operations;
    private final SchemaContext schemaContext;

    private DOMOperationRoutingTable(final Map<T, E> operations, final SchemaContext schemaContext) {
        this.operations = Preconditions.checkNotNull(operations);
        this.schemaContext = schemaContext;
    }

    protected abstract R newInstance(Map<T, E> operations, SchemaContext schemaContext);

    protected abstract ListMultimap<T, D> decomposeInstances(Set<I> instances);

    protected abstract E createOperationEntry(SchemaContext context, T type,
        Map<D, List<M>> implementations);

    public static final class DOMRpcRoutingTable extends DOMOperationRoutingTable<QName, YangInstanceIdentifier,
            DOMOperationImplementation.Rpc, DOMRpcRoutingTableEntry, Rpc, DOMRpcRoutingTable> {
        static final DOMRpcRoutingTable EMPTY = new DOMRpcRoutingTable(ImmutableMap.of(), null);

        private DOMRpcRoutingTable(Map<QName, DOMRpcRoutingTableEntry> operations, SchemaContext schemaContext) {
            super(operations, schemaContext);
        }

        @Override
        protected DOMRpcRoutingTable newInstance(Map<QName, DOMRpcRoutingTableEntry> operations,
                SchemaContext schemaContext) {
            return new DOMRpcRoutingTable(operations, schemaContext);
        }

        @Override
        protected ListMultimap<QName, YangInstanceIdentifier> decomposeInstances(
                Set<Rpc> instances) {
            final ListMultimap<QName, YangInstanceIdentifier> ret = LinkedListMultimap.create();
            for (Rpc rpc : instances) {
                ret.put(rpc.getType(), YangInstanceIdentifier.EMPTY);
            }
            return ret;
        }

        @Override
        protected DOMRpcRoutingTableEntry  createOperationEntry(SchemaContext context, QName type,
                Map<YangInstanceIdentifier, List<DOMOperationImplementation.Rpc>> implementations) {
            final RpcDefinition rpcDef = findRpcDefinition(context, type);
            if (rpcDef == null) {
                return null;
            }

            return rpcEntry(rpcDef.getQName(), implementations);
        }
    }

    public static final class DOMActionRoutingTable extends DOMOperationRoutingTable<SchemaPath, DOMDataTreeIdentifier,
            DOMOperationImplementation.Action, DOMActionRoutingTableEntry, DOMOperationInstance.Action,
            DOMActionRoutingTable> {
        static final DOMActionRoutingTable EMPTY = new DOMActionRoutingTable(ImmutableMap.of(), null);

        private DOMActionRoutingTable(Map<SchemaPath, DOMActionRoutingTableEntry> operations,
                SchemaContext schemaContext) {
            super(operations, schemaContext);
        }

        @Override
        protected DOMActionRoutingTable newInstance(
                Map<SchemaPath, DOMActionRoutingTableEntry> operations, SchemaContext schemaContext) {
            return new DOMActionRoutingTable(operations, schemaContext);
        }

        @Override
        protected ListMultimap<SchemaPath, DOMDataTreeIdentifier> decomposeInstances(
            final Set<Action> operations) {
            final ListMultimap<SchemaPath, DOMDataTreeIdentifier> ret = LinkedListMultimap.create();
            for (Action action : operations) {
                action.getDataTrees().forEach(id -> ret.put(action.getType(), id));
            }
            return ret;
        }

        @Override
        protected DOMActionRoutingTableEntry createOperationEntry(SchemaContext context,
                SchemaPath type, Map<DOMDataTreeIdentifier, List<DOMOperationImplementation.Action>> implementations) {
            final ActionDefinition actionDef = findActionDefinition(context, type);
            if (actionDef == null) {
                return null;
            }

            return actionEntry(type, implementations);
        }
    }

    R setSchemaContext(final SchemaContext context) {
        final Builder<T, E> b = ImmutableMap.builder();

        for (Entry<T, E> e : operations.entrySet()) {
            b.put(e.getKey(), createOperationEntry(context, e.getKey(), e.getValue().getImplementations()));
        }

        return newInstance(b.build(), context);
    }

    R add(final M implementation, final Set<I> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return (R) this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<T, D> toAdd = decomposeInstances(oprsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<T, E> mb = ImmutableMap.builder();
        for (Entry<T, E> re : this.operations.entrySet()) {
            List<D> newOprs = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOprs.isEmpty()) {
                final E ne = re.getValue().add(implementation, newOprs);
                mb.put(re.getKey(), ne);
            } else {
                mb.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<T, Collection<D>> e : toAdd.asMap().entrySet()) {
            final Builder<D, List<M>> vb = ImmutableMap.builder();
            final List<M> v = ImmutableList.of(implementation);
            for (D i : e.getValue()) {
                vb.put(i, v);
            }

            mb.put(e.getKey(), createOperationEntry(schemaContext, e.getKey(), vb.build()));
        }

        return newInstance(mb.build(), schemaContext);
    }

    R remove(final M implementation, final Set<I> instances) {
        if (instances.isEmpty()) {
            return (R) this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<T, D> toRemove = decomposeInstances(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<T, E> b = ImmutableMap.builder();
        for (Entry<T, E> e : this.operations.entrySet()) {
            final List<D> removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final E ne = e.getValue().remove(implementation, removed);
                if (ne != null) {
                    b.put(e.getKey(), ne);
                }
            } else {
                b.put(e);
            }
        }

        // All done, whatever is in toRemove, was not there in the first place
        return newInstance(b.build(), schemaContext);
    }

    @VisibleForTesting
    Map<T, Set<D>> getOperations() {
        return Maps.transformValues(operations, DOMOperationRoutingTableEntry::registeredIdentifiers);
    }

    Map<T, Set<D>> getOperations(final AvailabilityListener listener) {
        final Map<T, Set<D>> ret = new HashMap<>(operations.size());
        for (Entry<T, E> e : operations.entrySet()) {
            final Set<D> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    @Nullable E getEntry(final @NonNull T type) {
        return operations.get(type);
    }

    private static RpcDefinition findRpcDefinition(final SchemaContext context, final QName qname) {
        if (context != null) {
            final Module module = context.findModule(qname.getModule()).orElse(null);
            if (module != null && module.getRpcs() != null) {
                for (RpcDefinition rpc : module.getRpcs()) {
                    if (qname.equals(rpc.getQName())) {
                        return rpc;
                    }
                }
            }
        }

        return null;
    }

    private static ActionDefinition findActionDefinition(final SchemaContext context, final SchemaPath path) {
        //TODO
        return null;
    }
}
