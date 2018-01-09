/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

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
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

abstract class AbstractDOMOperationRoutingTableEntry {
    private final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> impls;
    private final SchemaPath schemaPath;

    AbstractDOMOperationRoutingTableEntry(final SchemaPath schemaPath, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> impls) {
        this.schemaPath = Preconditions.checkNotNull(schemaPath);
        this.impls = Preconditions.checkNotNull(impls);
    }

    final SchemaPath getSchemaPath() {
        return schemaPath;
    }

    final List<DOMOperationImplementation> getImplementations(final YangInstanceIdentifier context) {
        return impls.get(context);
    }

    final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> getImplementations() {
        return impls;
    }

    final Set<YangInstanceIdentifier> registeredIdentifiers(final DOMRpcAvailabilityListener listener) {
        return Maps.filterValues(impls, list -> list.stream().filter(impl -> impl instanceof DOMRpcImplementation)
            .map(DOMRpcImplementation.class::cast).anyMatch(listener::acceptsImplementation)).keySet();
    }


    final Set<YangInstanceIdentifier> registeredIdentifiers(final DOMOperationAvailabilityListener listener) {
        return Maps.filterValues(impls, list -> list.stream().filter(impl -> !(impl instanceof DOMRpcImplementation))
            .anyMatch(listener::acceptsImplementation)).keySet();
    }

    @VisibleForTesting
    final Set<YangInstanceIdentifier> registeredIdentifiers() {
        return impls.keySet();
    }

    /**
     * This method adds the given DOMImplementation instance for the given list RPC identifiers.
     *
     * @param implementation the DOMImplementation instance to add
     * @param newRpcs the List of new RPCs that the DOMImplementation provides, must be mutable
     * @return a new instance of AbstractDOMRpcRoutingTableEntry with the additions
     */
    final AbstractDOMOperationRoutingTableEntry add(final DOMOperationImplementation implementation,
            final List<YangInstanceIdentifier> newRpcs) {
        final Builder<YangInstanceIdentifier, List<DOMOperationImplementation>> vb = ImmutableMap.builder();
        for (final Entry<YangInstanceIdentifier, List<DOMOperationImplementation>> ve : impls.entrySet()) {
            if (newRpcs.remove(ve.getKey())) {
                final List<DOMOperationImplementation> i = new ArrayList<>(ve.getValue().size() + 1);
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
        for (final YangInstanceIdentifier ii : newRpcs) {
            final List<DOMOperationImplementation> impl = new ArrayList<>(1);
            impl.add(implementation);
            vb.put(ii, impl);
        }

        return newInstance(vb.build());
    }

    final AbstractDOMOperationRoutingTableEntry remove(final DOMOperationImplementation implementation,
            final List<YangInstanceIdentifier> removed) {
        final Builder<YangInstanceIdentifier, List<DOMOperationImplementation>> vb = ImmutableMap.builder();
        for (final Entry<YangInstanceIdentifier, List<DOMOperationImplementation>> ve : impls.entrySet()) {
            if (removed.remove(ve.getKey())) {
                final List<DOMOperationImplementation> i = new ArrayList<>(ve.getValue());
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

        final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> v = vb.build();
        return v.isEmpty() ? null : newInstance(v);
    }

    protected abstract AbstractDOMOperationRoutingTableEntry newInstance(
            Map<YangInstanceIdentifier, List<DOMOperationImplementation>> impls);
}
