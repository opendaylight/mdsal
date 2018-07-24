/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Abstract routing table definition for Action and RPC.
 * @param <I> instance type of RPC or Acton
 * @param <D> identifier type of RPC or Acton
 * @param <M> implementation type of RPC or Acton
 * @param <L> listener type of RPC or Acton
 * @param <E> routing entry type of RPC or Acton
 */
@Beta
abstract class AbstractDOMRoutingTable<I, D, M, L extends EventListener,
        E extends AbstractDOMRoutingTableEntry<D, M, L>> {
    private final Map<SchemaPath, E> operations;
    private final SchemaContext schemaContext;

    AbstractDOMRoutingTable(final Map<SchemaPath, E> operations,
            final SchemaContext schemaContext) {
        this.operations = Preconditions.checkNotNull(operations);
        this.schemaContext = schemaContext;
    }

    AbstractDOMRoutingTable setSchemaContext(final SchemaContext context) {
        final Builder<SchemaPath, E> b = ImmutableMap.builder();

        for (Entry<SchemaPath, E> e : operations.entrySet()) {
            final E entry = createOperationEntry(context, e.getKey(),
                e.getValue().getImplementations());
            if (entry != null) {
                b.put(e.getKey(), entry);
            }
        }

        return newInstance(b.build(), context);
    }

    AbstractDOMRoutingTable add(final M implementation, final Set<I> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, D> toAdd = decomposeIdentifiers(oprsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, E> mb = ImmutableMap.builder();
        for (Entry<SchemaPath, E> re : this.operations.entrySet()) {
            List<D> newOperations = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOperations.isEmpty()) {
                final E ne = (E) re.getValue().add(implementation, newOperations);
                mb.put(re.getKey(), ne);
            } else {
                mb.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<SchemaPath, Collection<D>> e : toAdd.asMap().entrySet()) {
            final Builder<D, List<M>> vb = ImmutableMap.builder();
            final List<M> v = ImmutableList.of(implementation);
            for (D i : e.getValue()) {
                vb.put(i, v);
            }

            final E entry = createOperationEntry(schemaContext, e.getKey(),
                vb.build());
            if (entry != null) {
                mb.put(e.getKey(), entry);
            }
        }

        return newInstance(mb.build(), schemaContext);
    }

    AbstractDOMRoutingTable remove(final M implementation, final Set<I> instances) {
        if (instances.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, D> toRemove = decomposeIdentifiers(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, E> b = ImmutableMap.builder();
        for (Entry<SchemaPath, E> e : this.operations.entrySet()) {
            final List<D> removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final E ne = (E) e.getValue().remove(implementation, removed);
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

//    boolean contains(final I input) {
//        final AbstractDOMRoutingTableEntry contexts = operations.get(input.getType());
//        return contexts != null && contexts.containsContext(input.getContextReference());
//    }

    @VisibleForTesting
    Map<SchemaPath, Set<D>> getOperations() {
        return Maps.transformValues(operations, AbstractDOMRoutingTableEntry::registeredIdentifiers);
    }

    Map<SchemaPath, Set<D>> getOperations(final L listener) {
        final Map<SchemaPath, Set<D>> ret = new HashMap<>(operations.size());
        for (Entry<SchemaPath, E> e : operations.entrySet()) {
            final Set<D> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    @Nullable AbstractDOMRoutingTableEntry getEntry(final @NonNull SchemaPath type) {
        return operations.get(type);
    }

    protected abstract AbstractDOMRoutingTable newInstance(
        Map<SchemaPath, E> operations, SchemaContext schemaContext);

    abstract ListMultimap<SchemaPath, D> decomposeIdentifiers(Set<I> instances);

//    private static ListMultimap<SchemaPath, D> decomposeIdentifiers(
//            final Set<I> rpcs) {
//        final ListMultimap<SchemaPath, D> ret = LinkedListMultimap.create();
//        for (I i : rpcs) {
//            ret.put(i.getType(), i.getContextReference());
//        }
//        return ret;
//    }

//    private static RpcDefinition findRpcDefinition(final SchemaContext context, final SchemaPath SchemaPath) {
//        if (context != null) {
//            final QName qname = SchemaPath.getPathFromRoot().iterator().next();
//            final Module module = context.findModule(qname.getModule()).orElse(null);
//            if (module != null && module.getRpcs() != null) {
//                for (RpcDefinition rpc : module.getRpcs()) {
//                    if (qname.equals(rpc.getQName())) {
//                        return rpc;
//                    }
//                }
//            }
//        }
//
//        return null;
//    }

    abstract E createOperationEntry(SchemaContext context, SchemaPath key,
        Map<D, List<M>> implementations);
//    private static AbstractDOMRoutingTableEntry createRpcEntry(final SchemaContext context, final SchemaPath key,
//            final Map<D, List<M>> implementations) {
//        final RpcDefinition rpcDef = findRpcDefinition(context, key);
//        if (rpcDef == null) {
//            return new UnknownDOMRpcRoutingTableEntry(key, implementations);
//        }
//
//        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(rpcDef);
//        if (strategy.isContextBasedRouted()) {
//            return new RoutedDOMRpcRoutingTableEntry(rpcDef, D.of(strategy.getLeaf()),
//                implementations);
//        }
//
//        return new GlobalDOMRpcRoutingTableEntry(rpcDef, implementations);
//    }
}
