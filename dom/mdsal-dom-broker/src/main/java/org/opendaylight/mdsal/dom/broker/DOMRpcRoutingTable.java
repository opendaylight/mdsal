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
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class DOMRpcRoutingTable extends AbstractDOMRoutingTable<DOMRpcIdentifier, YangInstanceIdentifier,
        DOMRpcImplementation, DOMRpcAvailabilityListener, AbstractDOMRpcRoutingTableEntry> {
    static final DOMRpcRoutingTable EMPTY = new DOMRpcRoutingTable(ImmutableMap.of(), null);

    private DOMRpcRoutingTable(final Map<SchemaPath, AbstractDOMRpcRoutingTableEntry> rpcs,
            final EffectiveModelContext schemaContext) {
        super(rpcs, schemaContext);
    }

    boolean contains(final DOMRpcIdentifier input) {
        final AbstractDOMRpcRoutingTableEntry contexts = (AbstractDOMRpcRoutingTableEntry) getEntry(input.getType());
        return contexts != null && contexts.containsContext(input.getContextReference());
    }

    @Override
    protected DOMRpcRoutingTable newInstance(final Map<SchemaPath, AbstractDOMRpcRoutingTableEntry> operations,
            final EffectiveModelContext schemaContext) {
        return new DOMRpcRoutingTable(operations, schemaContext);
    }

    @Override
    protected ListMultimap<SchemaPath, YangInstanceIdentifier> decomposeIdentifiers(
            final Set<DOMRpcIdentifier> rpcs) {
        final ListMultimap<SchemaPath, YangInstanceIdentifier> ret = LinkedListMultimap.create();
        for (DOMRpcIdentifier i : rpcs) {
            ret.put(i.getType(), i.getContextReference());
        }
        return ret;
    }

    @Override
    AbstractDOMRpcRoutingTableEntry createOperationEntry(final EffectiveModelContext context, final SchemaPath key,
            final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> implementations) {
        final RpcDefinition rpcDef = findRpcDefinition(context, key);
        if (rpcDef == null) {
            return new UnknownDOMRpcRoutingTableEntry(key, implementations);
        }

        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(rpcDef);
        if (strategy.isContextBasedRouted()) {
            return new RoutedDOMRpcRoutingTableEntry(rpcDef, YangInstanceIdentifier.of(strategy.getLeaf()),
                implementations);
        }

        return new GlobalDOMRpcRoutingTableEntry(rpcDef, implementations);
    }

    private static RpcDefinition findRpcDefinition(final SchemaContext context, final SchemaPath schemaPath) {
        if (context != null) {
            final QName qname = schemaPath.getPathFromRoot().iterator().next();
            final Module module = context.findModule(qname.getModule()).orElse(null);
            if (module != null && module.getRpcs() != null) {
                for (RpcDefinition rpc : module.getRpcs()) {
                    if (qname.equals(rpc.getQName())) {
                        return rpc;
                    }
                }
            }
        }

        return null;
    }
}
