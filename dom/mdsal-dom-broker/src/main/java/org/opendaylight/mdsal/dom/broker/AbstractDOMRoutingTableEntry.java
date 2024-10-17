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
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract routing table entry definition for Action and RPC.
 *
 * @param <D> identifier type of RPC or Acton
 * @param <M> implementation type of RPC or Acton
 * @param <L> listener type of RPC or Acton
 * @param <K> routing key type
 */
abstract sealed class AbstractDOMRoutingTableEntry<D, M, L, K>
        permits DOMActionRoutingTableEntry, DOMRpcRoutingTableEntry {
    private final Map<D, List<M>> implementations;
    private final K type;

    AbstractDOMRoutingTableEntry(final K type, final Map<D, List<M>> implementations) {
        this.type = requireNonNull(type);
        this.implementations = requireNonNull(implementations);
    }

    final K getType() {
        return type;
    }

    final List<M> getImplementations(final D identifier) {
        return implementations.get(identifier);
    }

    final Map<D, List<M>> getImplementations() {
        return implementations;
    }

    @VisibleForTesting
    final Set<D> registeredIdentifiers() {
        return implementations.keySet();
    }

    protected abstract Set<D> registeredIdentifiers(L listener);

    /**
     * This method adds the given DOMOperationImplementation instance for the given list operation identifiers.
     *
     * @param implementation the DOMOperationImplementation instance to add
     * @param newOprs  the List of new RPCs/Actions that the DOMOperationImplementation provides, must be mutable
     * @return a new instance of DOMActionRoutingTableEntry with the additions
     */
    final AbstractDOMRoutingTableEntry<D, M, L, K> add(final M implementation, final List<D> newOprs) {
        final var vb = ImmutableMap.<D, List<M>>builder();
        for (var entry : implementations.entrySet()) {
            if (newOprs.remove(entry.getKey())) {
                final var impls = new ArrayList<M>(entry.getValue().size() + 1);
                impls.addAll(entry.getValue());
                impls.add(implementation);

                // New implementation is at the end, this will move it to be the last among implementations
                // with equal cost -- relying on sort() being stable.
                impls.sort(implComparator());
                vb.put(entry.getKey(), impls);
            } else {
                vb.put(entry);
            }
        }
        for (var ii : newOprs) {
            final var impl = new ArrayList<M>(1);
            impl.add(implementation);
            vb.put(ii, impl);
        }

        return newInstance(vb.build());
    }

    final AbstractDOMRoutingTableEntry<D, M, L, K> remove(final M implementation, final List<D> removed) {
        final var vb = ImmutableMap.<D, List<M>>builder();
        for (var entry : implementations.entrySet()) {
            if (removed.remove(entry.getKey())) {
                final var impls = new ArrayList<>(entry.getValue());
                impls.remove(implementation);
                // We could trimToSize(), but that may perform another copy just to get rid
                // of a single element. That is probably not worth the trouble.
                if (!impls.isEmpty()) {
                    vb.put(entry.getKey(), impls);
                }
            } else {
                vb.put(entry);
            }
        }

        final var v = vb.build();
        return v.isEmpty() ? null : newInstance(vb.build());
    }

    protected abstract Comparator<M> implComparator();

    protected abstract AbstractDOMRoutingTableEntry<D, M, L, K> newInstance(Map<D, List<M>> impls);
}
