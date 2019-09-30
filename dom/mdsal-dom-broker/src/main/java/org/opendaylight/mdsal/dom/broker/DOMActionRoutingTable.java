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
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;


/**
 * Definition of Action routing table.
 */
@Beta
final class DOMActionRoutingTable extends AbstractDOMRoutingTable<DOMActionInstance, DOMDataTreeIdentifier,
        DOMActionImplementation, AvailabilityListener, DOMActionRoutingTableEntry> {
    static final DOMActionRoutingTable EMPTY = new DOMActionRoutingTable(ImmutableMap.of(), null);

    private DOMActionRoutingTable(final Map<SchemaPath, DOMActionRoutingTableEntry> actions,
            final EffectiveModelContext schemaContext) {
        super(actions, schemaContext);
    }

    @Override
    protected DOMActionRoutingTable newInstance(final Map<SchemaPath, DOMActionRoutingTableEntry> operations,
            final EffectiveModelContext schemaContext) {
        return new DOMActionRoutingTable(operations, schemaContext);
    }

    @Override
    protected ListMultimap<SchemaPath, DOMDataTreeIdentifier> decomposeIdentifiers(
            final Set<DOMActionInstance> instances) {
        final ListMultimap<SchemaPath, DOMDataTreeIdentifier> ret = LinkedListMultimap.create();
        for (DOMActionInstance instance : instances) {
            instance.getDataTrees().forEach(id -> ret.put(instance.getType(), id));
        }
        return ret;
    }

    @Override
    protected DOMActionRoutingTableEntry createOperationEntry(final EffectiveModelContext context,
            final SchemaPath type, final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations) {
        final ActionDefinition actionDef = findActionDefinition(context, type);
        if (actionDef == null) {
            //FIXME: return null directly instead of providing kind of unknown entry.
            return null;
        }

        return new DOMActionRoutingTableEntry(type, implementations);
    }

    private static ActionDefinition findActionDefinition(final SchemaContext context, final SchemaPath path) {
        final SchemaNode node = SchemaContextUtil.findDataSchemaNode(context, path.getParent());
        if (node instanceof ActionNodeContainer) {
            return ((ActionNodeContainer) node).findAction(path.getLastComponent()).orElse(null);
        }
        return null;
    }
}
