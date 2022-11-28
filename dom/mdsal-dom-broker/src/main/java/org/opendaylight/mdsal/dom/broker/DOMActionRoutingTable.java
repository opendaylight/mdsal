/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Definition of Action routing table.
 */
@Beta
final class DOMActionRoutingTable extends AbstractDOMRoutingTable<DOMActionInstance, DOMDataTreeIdentifier,
        DOMActionImplementation, AvailabilityListener, Absolute, DOMActionRoutingTableEntry> {
    static final DOMActionRoutingTable EMPTY = new DOMActionRoutingTable(ImmutableMap.of(), null);

    private DOMActionRoutingTable(final Map<Absolute, DOMActionRoutingTableEntry> actions,
            final EffectiveModelContext schemaContext) {
        super(actions, schemaContext);
    }

    @Override
    protected DOMActionRoutingTable newInstance(final Map<Absolute, DOMActionRoutingTableEntry> operations,
            final EffectiveModelContext schemaContext) {
        return new DOMActionRoutingTable(operations, schemaContext);
    }

    @Override
    protected Entry<Absolute, DOMDataTreeIdentifier> decomposeIdentifier(final DOMActionInstance instance) {
        // Not used
        throw new UnsupportedOperationException();
    }

    @Override
    protected ListMultimap<Absolute, DOMDataTreeIdentifier> decomposeIdentifiers(
            final Set<DOMActionInstance> instances) {
        final ListMultimap<Absolute, DOMDataTreeIdentifier> ret = LinkedListMultimap.create();
        for (DOMActionInstance instance : instances) {
            instance.getDataTrees().forEach(id -> ret.put(instance.getType(), id));
        }
        return ret;
    }

    @Override
    protected DOMActionRoutingTableEntry createOperationEntry(final EffectiveModelContext context,
            final Absolute type, final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations) {
        return context.findSchemaTreeNode(ActionDefinition.class, type)
            .map(dummy -> new DOMActionRoutingTableEntry(type, implementations))
            .orElse(null);
    }
}
