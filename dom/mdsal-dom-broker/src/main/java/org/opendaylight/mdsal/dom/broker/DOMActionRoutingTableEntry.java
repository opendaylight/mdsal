/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Definition of Action routing table entry.
 */
@Beta
final class DOMActionRoutingTableEntry {
    private final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations;
    private final SchemaPath type;

    DOMActionRoutingTableEntry(final SchemaPath type,
            final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations) {
        this.type = Preconditions.checkNotNull(type);
        this.implementations = Preconditions.checkNotNull(implementations);
    }

    SchemaPath getType() {
        return type;
    }

    List<DOMActionImplementation> getImplementations(final DOMDataTreeIdentifier identifier) {
        return implementations.get(identifier);
    }

    Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> getImplementations() {
        return implementations;
    }

    Set<DOMDataTreeIdentifier> registeredIdentifiers(final AvailabilityListener listener) {
        return Maps.filterValues(implementations, list -> list.stream()
            .anyMatch(listener::acceptsImplementation)).keySet();
    }

    @VisibleForTesting
    Set<DOMDataTreeIdentifier> registeredIdentifiers() {
        return implementations.keySet();
    }

    /**
     * This method adds the given DOMOperationImplementation instance for the given list operation identifiers.
     *
     * @param implementation the DOMOperationImplementation instance to add
     * @param newActions  the List of new RPCs/Actions that the DOMOperationImplementation provides, must be mutable
     * @return a new instance of DOMActionRoutingTableEntry with the additions
     */
    DOMActionRoutingTableEntry add(final DOMActionImplementation implementation,
            final List<DOMDataTreeIdentifier> newActions) {
        final Builder<DOMDataTreeIdentifier, List<DOMActionImplementation>> vb = ImmutableMap.builder();
        for (final Entry<DOMDataTreeIdentifier, List<DOMActionImplementation>> ve : implementations.entrySet()) {
            if (newActions.remove(ve.getKey())) {
                final List<DOMActionImplementation> i = new ArrayList<>(ve.getValue().size() + 1);
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
        for (final DOMDataTreeIdentifier ii : newActions) {
            final List<DOMActionImplementation> impl = new ArrayList<>(1);
            impl.add(implementation);
            vb.put(ii, impl);
        }

        return newInstance(vb.build());
    }

    DOMActionRoutingTableEntry remove(final DOMActionImplementation implementation,
            final List<DOMDataTreeIdentifier> removed) {
        final Builder<DOMDataTreeIdentifier, List<DOMActionImplementation>> vb = ImmutableMap.builder();
        for (final Entry<DOMDataTreeIdentifier, List<DOMActionImplementation>> ve : implementations.entrySet()) {
            if (removed.remove(ve.getKey())) {
                final List<DOMActionImplementation> i = new ArrayList<>(ve.getValue());
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

        final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> v = vb.build();
        return v.isEmpty() ? null : newInstance(vb.build());
    }

    private DOMActionRoutingTableEntry newInstance(
        Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> impls) {
        return new DOMActionRoutingTableEntry(getType(), impls);
    }
}
