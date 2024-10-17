/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Abstract routing table definition for Action and RPC.
 *
 * @param <I> instance type of RPC or Acton
 * @param <D> identifier type of RPC or Acton
 * @param <M> implementation type of RPC or Acton
 * @param <L> listener type of RPC or Acton
 * @param <E> routing entry type of RPC or Acton
 * @param <K> routing key type
 */
abstract sealed class AbstractDOMRoutingTable<I, D, M, L, K, E extends AbstractDOMRoutingTableEntry<D, M, L, K>>
        permits DOMActionRoutingTable, DOMRpcRoutingTable {
    private final Map<K, E> operations;
    private final EffectiveModelContext schemaContext;

    AbstractDOMRoutingTable(final Map<K, E> operations, final EffectiveModelContext schemaContext) {
        this.operations = requireNonNull(operations);
        this.schemaContext = schemaContext;
    }

    final AbstractDOMRoutingTable<I, D, M, L, K, E> setSchemaContext(final EffectiveModelContext context) {
        final var builder = ImmutableMap.<K, E>builder();

        for (var oper : operations.entrySet()) {
            final var entry = createOperationEntry(context, oper.getKey(), oper.getValue().getImplementations());
            if (entry != null) {
                builder.put(oper.getKey(), entry);
            }
        }

        return newInstance(builder.build(), context);
    }

    final AbstractDOMRoutingTable<I, D, M, L, K, E> add(final M implementation, final Set<I> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final var toAdd = decomposeIdentifiers(oprsToAdd);

        final var builder = ImmutableMap.<K, E>builder();

        // Now iterate over existing entries, modifying them as appropriate...
        for (var re : operations.entrySet()) {
            final var newOperations = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOperations.isEmpty()) {
                final var ne = (E) re.getValue().add(implementation, newOperations);
                builder.put(re.getKey(), ne);
            } else {
                builder.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (var e : toAdd.asMap().entrySet()) {
            final var vb = ImmutableMap.<D, List<M>>builder();
            final var v = ImmutableList.of(implementation);
            for (var i : e.getValue()) {
                vb.put(i, v);
            }

            final var entry = createOperationEntry(schemaContext, e.getKey(), vb.build());
            if (entry != null) {
                builder.put(e.getKey(), entry);
            }
        }

        return newInstance(builder.build(), schemaContext);
    }

    final AbstractDOMRoutingTable<I, D, M, L, K, E> addAll(final ImmutableTable<K, D, M> impls) {
        if (impls.isEmpty()) {
            return this;
        }

        // Create a temporary map, which we will mutatate
        final var toAdd = HashBasedTable.create(impls);
        final var mb = ImmutableMap.<K, E>builder();

        // Now iterate over existing entries, modifying them as appropriate...
        for (var re : operations.entrySet()) {
            final var newImpls = toAdd.rowMap().remove(re.getKey());
            if (newImpls == null) {
                mb.put(re);
                continue;
            }

            var ne = re;
            for (var oper : newImpls.entrySet()) {
                @SuppressWarnings("unchecked")
                final E newVal = (E) ne.getValue().add(oper.getValue(), Lists.newArrayList(oper.getKey()));
                ne = Map.entry(ne.getKey(), newVal);
            }
            mb.put(ne);
        }

        // Finally add whatever is left in the decomposed multimap
        for (var e : toAdd.rowMap().entrySet()) {
            final var entry = createOperationEntry(schemaContext, e.getKey(), ImmutableMap.copyOf(
                Maps.<D, M, List<M>>transformValues(e.getValue(), ImmutableList::of)));
            if (entry != null) {
                mb.put(e.getKey(), entry);
            }
        }

        return newInstance(mb.build(), schemaContext);
    }

    final AbstractDOMRoutingTable<I, D, M, L, K, E> remove(final M implementation, final Set<I> instances) {
        if (instances.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final var toRemove = decomposeIdentifiers(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final var builder = ImmutableMap.<K, E>builder();
        for (var e : operations.entrySet()) {
            final var removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final E ne = (E) e.getValue().remove(implementation, removed);
                if (ne != null) {
                    builder.put(e.getKey(), ne);
                }
            } else {
                builder.put(e);
            }
        }

        // All done, whatever is in toRemove, was not there in the first place
        return newInstance(builder.build(), schemaContext);
    }

    final AbstractDOMRoutingTable<I, D, M, L, K, E> removeAll(final ImmutableTable<K, D, M> impls) {
        if (impls.isEmpty()) {
            return this;
        }

        // Create a temporary map, which we will mutatate
        final var toRemove = HashBasedTable.create(impls);
        final var mb = ImmutableMap.<K, E>builder();

        // Now iterate over existing entries, modifying them as appropriate...
        for (var re : operations.entrySet()) {
            final var oldImpls = toRemove.rowMap().remove(re.getKey());
            if (oldImpls == null) {
                mb.put(re);
                continue;
            }

            var ne = re;
            for (var oper : oldImpls.entrySet()) {
                if (ne != null) {
                    @SuppressWarnings("unchecked")
                    final E newVal = (E) ne.getValue().remove(oper.getValue(), Lists.newArrayList(oper.getKey()));
                    if (newVal != null) {
                        ne = Map.entry(ne.getKey(), newVal);
                    } else {
                        ne = null;
                    }
                }
            }
            if (ne != null) {
                mb.put(ne);
            }
        }

        // All done, whatever is in toRemove, was not there in the first place
        return newInstance(mb.build(), schemaContext);
    }

    static final <K, V> HashMultimap<V, K> invertImplementationsMap(final Map<K, V> map) {
        return Multimaps.invertFrom(Multimaps.forMap(map), HashMultimap.create());
    }

    @VisibleForTesting
    final Map<K, Set<D>> getOperations() {
        return Maps.transformValues(operations, AbstractDOMRoutingTableEntry::registeredIdentifiers);
    }

    final Map<K, Set<D>> getOperations(final L listener) {
        final var ret = new HashMap<K, Set<D>>(operations.size());
        for (var e : operations.entrySet()) {
            final var ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }
        return ret;
    }

    final @Nullable AbstractDOMRoutingTableEntry<D, M, L, K> getEntry(final @NonNull K type) {
        return operations.get(type);
    }

    protected abstract AbstractDOMRoutingTable<I, D, M, L, K, E> newInstance(Map<K, E> operations,
            EffectiveModelContext schemaContext);

    abstract ListMultimap<K, D> decomposeIdentifiers(Set<I> instances);

    abstract @Nullable E createOperationEntry(EffectiveModelContext context, K key, Map<D, List<M>> implementations);
}
