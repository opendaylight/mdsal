/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
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
@Beta
abstract class AbstractDOMRoutingTable<I, D, M, L extends EventListener, K,
        E extends AbstractDOMRoutingTableEntry<D, M, L, K>> {
    private final Map<K, E> operations;
    private final EffectiveModelContext schemaContext;

    AbstractDOMRoutingTable(final Map<K, E> operations, final EffectiveModelContext schemaContext) {
        this.operations = requireNonNull(operations);
        this.schemaContext = schemaContext;
    }

    AbstractDOMRoutingTable<I, D, M, L, K, E> setSchemaContext(final EffectiveModelContext context) {
        final Builder<K, E> b = ImmutableMap.builder();

        for (Entry<K, E> e : operations.entrySet()) {
            final E entry = createOperationEntry(context, e.getKey(), e.getValue().getImplementations());
            if (entry != null) {
                b.put(e.getKey(), entry);
            }
        }

        return newInstance(b.build(), context);
    }

    AbstractDOMRoutingTable<I, D, M, L, K, E> add(final M implementation, final Set<I> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return this;
        }

        final Builder<K, E> mb = ImmutableMap.builder();
        add(implementation, oprsToAdd, mb);

        return newInstance(mb.build(), schemaContext);
    }

    private void add(final M implementation, final Set<I> oprsToAdd,
            final Builder<K, E> tableInputBuilder) {
        // First decompose the identifiers to a multimap
        final ListMultimap<K, D> toAdd = decomposeIdentifiers(oprsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        for (Entry<K, E> re : this.operations.entrySet()) {
            List<D> newOperations = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOperations.isEmpty()) {
                final E ne = (E) re.getValue().add(implementation, newOperations);
                tableInputBuilder.put(re.getKey(), ne);
            } else {
                tableInputBuilder.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<K, Collection<D>> e : toAdd.asMap().entrySet()) {
            final Builder<D, List<M>> vb = ImmutableMap.builder();
            final List<M> v = ImmutableList.of(implementation);
            for (D i : e.getValue()) {
                vb.put(i, v);
            }

            final E entry = createOperationEntry(schemaContext, e.getKey(), vb.build());
            if (entry != null) {
                tableInputBuilder.put(e.getKey(), entry);
            }
        }
    }

    AbstractDOMRoutingTable<I, D, M, L, K, E> addAll(final Map<I, M> map) {
        if (map.isEmpty()) {
            return this;
        }

        HashMultimap<M, I> inverted = invertImplementationsMap(map);

        final Builder<K, E> tableInputBuilder = ImmutableMap.builder();
        for (M impl : inverted.keySet()) {
            add(impl, inverted.get(impl), tableInputBuilder);
        }

        return newInstance(tableInputBuilder.build(), schemaContext);
    }

    AbstractDOMRoutingTable<I, D, M, L, K, E> remove(final M implementation, final Set<I> instances) {
        if (instances.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<K, D> toRemove = decomposeIdentifiers(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<K, E> b = ImmutableMap.builder();
        for (Entry<K, E> e : this.operations.entrySet()) {
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


    private void remove(final M implementation, final Set<I> oprsToRemove,
                        final Builder<K, E> tableInputBuilder) {
        final ListMultimap<K, D> toRemove = decomposeIdentifiers(oprsToRemove);

        for (Entry<K, E> e : this.operations.entrySet()) {
            final List<D> removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final E ne = (E) e.getValue().remove(implementation, removed);
                if (ne != null) {
                    tableInputBuilder.put(e.getKey(), ne);
                }
            } else {
                tableInputBuilder.put(e);
            }
        }
    }

    AbstractDOMRoutingTable<I, D, M, L, K, E> removeAll(final Map<I, M> map) {
        if (map.isEmpty()) {
            return this;
        }
        HashMultimap<M, I> inverted = invertImplementationsMap(map);

        final Builder<K, E> tableInputBuilder = ImmutableMap.builder();
        for (M impl : inverted.keySet()) {
            remove(impl, inverted.get(impl), tableInputBuilder);
        }

        return newInstance(tableInputBuilder.build(), schemaContext);
    }

    static final <K, V> HashMultimap<V, K> invertImplementationsMap(final Map<K, V> map) {
        return Multimaps.invertFrom(Multimaps.forMap(map), HashMultimap.create());
    }

    @VisibleForTesting
    Map<K, Set<D>> getOperations() {
        return Maps.transformValues(operations, AbstractDOMRoutingTableEntry::registeredIdentifiers);
    }

    Map<K, Set<D>> getOperations(final L listener) {
        final Map<K, Set<D>> ret = new HashMap<>(operations.size());
        for (Entry<K, E> e : operations.entrySet()) {
            final Set<D> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    @Nullable AbstractDOMRoutingTableEntry<D, M, L, K> getEntry(final @NonNull K type) {
        return operations.get(type);
    }

    protected abstract AbstractDOMRoutingTable<I, D, M, L, K, E> newInstance(Map<K, E> operations,
            EffectiveModelContext schemaContext);

    abstract ListMultimap<K, D> decomposeIdentifiers(Set<I> instances);

    abstract @Nullable E createOperationEntry(EffectiveModelContext context, K key, Map<D, List<M>> implementations);
}
