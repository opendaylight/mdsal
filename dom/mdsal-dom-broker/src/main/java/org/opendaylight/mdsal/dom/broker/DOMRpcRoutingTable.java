/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

final class DOMRpcRoutingTable extends AbstractDOMRoutingTable<DOMRpcIdentifier, YangInstanceIdentifier,
        DOMRpcImplementation, DOMRpcAvailabilityListener, QName, AbstractDOMRpcRoutingTableEntry> {
    static final DOMRpcRoutingTable EMPTY = new DOMRpcRoutingTable(ImmutableMap.of(), null);

    private DOMRpcRoutingTable(final Map<QName, AbstractDOMRpcRoutingTableEntry> rpcs,
            final EffectiveModelContext schemaContext) {
        super(rpcs, schemaContext);
    }

    boolean contains(final DOMRpcIdentifier input) {
        final AbstractDOMRpcRoutingTableEntry contexts = (AbstractDOMRpcRoutingTableEntry) getEntry(input.getType());
        return contexts != null && contexts.containsContext(input.getContextReference());
    }

    @Override
    protected DOMRpcRoutingTable newInstance(final Map<QName, AbstractDOMRpcRoutingTableEntry> operations,
            final EffectiveModelContext schemaContext) {
        return new DOMRpcRoutingTable(operations, schemaContext);
    }

    @Override
    protected Entry<QName, YangInstanceIdentifier> decomposeIdentifier(final DOMRpcIdentifier instance) {
        return Map.entry(instance.getType(), instance.getContextReference());
    }

    @Override
    protected ListMultimap<QName, YangInstanceIdentifier> decomposeIdentifiers(final Set<DOMRpcIdentifier> rpcs) {
        final ListMultimap<QName, YangInstanceIdentifier> ret = LinkedListMultimap.create();
        for (DOMRpcIdentifier i : rpcs) {
            ret.put(i.getType(), i.getContextReference());
        }
        return ret;
    }

    @Override
    AbstractDOMRpcRoutingTableEntry createOperationEntry(final EffectiveModelContext context, final QName key,
            final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> implementations) {
        final var rpcDef = findRpcDefinition(context, key);
        if (rpcDef == null) {
            return new UnknownDOMRpcRoutingTableEntry(key, implementations);
        }

        final var contentContext = ContentRoutedRpcContext.forRpc(rpcDef);
        return contentContext == null ? new GlobalDOMRpcRoutingTableEntry(rpcDef.argument(), implementations)
            : new RoutedDOMRpcRoutingTableEntry(rpcDef.argument(), YangInstanceIdentifier.of(contentContext.leaf()),
                implementations);
    }

    private static @Nullable RpcEffectiveStatement findRpcDefinition(final EffectiveModelContext context,
            final QName qname) {
        return context == null ? null : context.findModuleStatement(qname.getModule())
            .flatMap(module -> module.findSchemaTreeNode(qname))
            .filter(RpcEffectiveStatement.class::isInstance)
            .map(RpcEffectiveStatement.class::cast)
            .orElse(null);
    }
}
