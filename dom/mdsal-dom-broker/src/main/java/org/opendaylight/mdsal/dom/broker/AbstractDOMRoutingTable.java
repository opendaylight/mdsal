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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

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
    private final Map<Absolute, E> operations;
    private final EffectiveModelContext schemaContext;

    AbstractDOMRoutingTable(final Map<Absolute, E> operations, final EffectiveModelContext schemaContext) {
        this.operations = requireNonNull(operations);
        this.schemaContext = schemaContext;
    }

    AbstractDOMRoutingTable<I, D, M, L, E> setSchemaContext(final EffectiveModelContext context) {
        final Builder<Absolute, E> b = ImmutableMap.builder();

        for (Entry<Absolute, E> e : operations.entrySet()) {
            final E entry = createOperationEntry(context, e.getKey(), e.getValue().getImplementations());
            if (entry != null) {
                b.put(e.getKey(), entry);
            }
        }

        return newInstance(b.build(), context);
    }

    AbstractDOMRoutingTable<I, D, M, L, E> add(final M implementation, final Set<I> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<Absolute, D> toAdd = decomposeIdentifiers(oprsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<Absolute, E> mb = ImmutableMap.builder();
        for (Entry<Absolute, E> re : this.operations.entrySet()) {
            List<D> newOperations = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOperations.isEmpty()) {
                final E ne = (E) re.getValue().add(implementation, newOperations);
                mb.put(re.getKey(), ne);
            } else {
                mb.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<Absolute, Collection<D>> e : toAdd.asMap().entrySet()) {
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

    AbstractDOMRoutingTable<I, D, M, L, E> remove(final M implementation, final Set<I> instances) {
        if (instances.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<Absolute, D> toRemove = decomposeIdentifiers(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<Absolute, E> b = ImmutableMap.builder();
        for (Entry<Absolute, E> e : this.operations.entrySet()) {
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

    @VisibleForTesting
    Map<Absolute, Set<D>> getOperations() {
        return Maps.transformValues(operations, AbstractDOMRoutingTableEntry::registeredIdentifiers);
    }

    Map<Absolute, Set<D>> getOperations(final L listener) {
        final Map<Absolute, Set<D>> ret = new HashMap<>(operations.size());
        for (Entry<Absolute, E> e : operations.entrySet()) {
            final Set<D> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    @Nullable AbstractDOMRoutingTableEntry<D, M, L> getEntry(final @NonNull Absolute type) {
        return operations.get(type);
    }

    protected abstract AbstractDOMRoutingTable<I, D, M, L, E> newInstance(Map<Absolute, E> operations,
            EffectiveModelContext schemaContext);

    abstract ListMultimap<Absolute, D> decomposeIdentifiers(Set<I> instances);

    abstract E createOperationEntry(EffectiveModelContext context, Absolute key, Map<D, List<M>> implementations);
}
