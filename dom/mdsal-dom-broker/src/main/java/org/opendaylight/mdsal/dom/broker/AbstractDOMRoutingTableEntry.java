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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Abstract routing table entry definition for Action and RPC.
 * @param <D> identifier type of RPC or Acton
 * @param <M> implementation type of RPC or Acton
 * @param <L> listener type of RPC or Acton
 */
@Beta
abstract class AbstractDOMRoutingTableEntry<D, M, L extends EventListener> {
    private final Map<D, List<M>> implementations;
    private final SchemaPath type;

    AbstractDOMRoutingTableEntry(final SchemaPath type, final Map<D, List<M>> implementations) {
        this.type = requireNonNull(type);
        this.implementations = requireNonNull(implementations);
    }

    SchemaPath getType() {
        return type;
    }

    List<M> getImplementations(final D identifier) {
        return implementations.get(identifier);
    }

    Map<D, List<M>> getImplementations() {
        return implementations;
    }

    @VisibleForTesting
    Set<D> registeredIdentifiers() {
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
    AbstractDOMRoutingTableEntry<D, M, L> add(final M implementation, final List<D> newOprs) {
        final Builder<D, List<M>> vb = ImmutableMap.builder();
        for (final Entry<D, List<M>> ve : implementations.entrySet()) {
            if (newOprs.remove(ve.getKey())) {
                final List<M> i = new ArrayList<>(ve.getValue().size() + 1);
                i.addAll(ve.getValue());
                i.add(implementation);

                // New implementation is at the end, this will move it to be the last among implementations
                // with equal cost -- relying on sort() being stable.
                i.sort(implComparator());
                vb.put(ve.getKey(), i);
            } else {
                vb.put(ve);
            }
        }
        for (final D ii : newOprs) {
            final List<M> impl = new ArrayList<>(1);
            impl.add(implementation);
            vb.put(ii, impl);
        }

        return newInstance(vb.build());
    }

    AbstractDOMRoutingTableEntry<D, M, L> remove(final M implementation, final List<D> removed) {
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

    protected abstract Comparator<M> implComparator();

    protected abstract AbstractDOMRoutingTableEntry<D, M, L> newInstance(Map<D, List<M>> impls);
}
