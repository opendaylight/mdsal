/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

abstract class AbstractDOMRpcRoutingTableEntry extends AbstractDOMRoutingTableEntry<YangInstanceIdentifier,
        DOMRpcImplementation, DOMRpcAvailabilityListener> {
    private final DOMRpcIdentifier rpcId;

    AbstractDOMRpcRoutingTableEntry(final DOMRpcIdentifier rpcId, final Map<YangInstanceIdentifier,
        List<DOMRpcImplementation>> implementations) {
        super(rpcId.getType(), implementations);
        this.rpcId = requireNonNull(rpcId);
    }

    final DOMRpcIdentifier getRpcId() {
        return rpcId;
    }

    final boolean containsContext(final YangInstanceIdentifier contextReference) {
        return getImplementations().containsKey(contextReference);
    }

    @Override
    protected final Set<YangInstanceIdentifier> registeredIdentifiers(final DOMRpcAvailabilityListener listener) {
        return Maps.filterValues(getImplementations(), list -> list.stream()
            .anyMatch(listener::acceptsImplementation)).keySet();
    }

    @Override
    protected Comparator<DOMRpcImplementation> implComparator() {
        return Comparator.comparingLong(DOMRpcImplementation::invocationCost);
    }
}
