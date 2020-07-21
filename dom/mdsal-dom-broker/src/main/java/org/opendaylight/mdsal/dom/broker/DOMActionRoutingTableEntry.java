/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Definition of Action routing table entry.
 */
@Beta
final class DOMActionRoutingTableEntry extends AbstractDOMRoutingTableEntry<DOMDataTreeIdentifier,
        DOMActionImplementation, AvailabilityListener> {
    DOMActionRoutingTableEntry(final Absolute type,
            final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations) {
        super(type, implementations);
    }

    @Override
    protected Set<DOMDataTreeIdentifier> registeredIdentifiers(final AvailabilityListener listener) {
        return Maps.filterValues(getImplementations(), list -> list.stream()
            .anyMatch(listener::acceptsImplementation)).keySet();
    }

    @Override
    protected Comparator<DOMActionImplementation> implComparator() {
        return Comparator.comparingLong(DOMActionImplementation::invocationCost);
    }

    @Override
    protected DOMActionRoutingTableEntry newInstance(
            final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> impls) {
        return new DOMActionRoutingTableEntry(getType(), impls);
    }
}
