/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMOperationAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

final class DOMRpcRoutingTable {
    static final DOMRpcRoutingTable EMPTY = new DOMRpcRoutingTable(ImmutableMap.of(), null);

    private final Map<SchemaPath, AbstractDOMOperationRoutingTableEntry> rpcs;
    private final SchemaContext schemaContext;

    private DOMRpcRoutingTable(final Map<SchemaPath, AbstractDOMOperationRoutingTableEntry> rpcs,
            final SchemaContext schemaContext) {
        this.rpcs = Preconditions.checkNotNull(rpcs);
        this.schemaContext = schemaContext;
    }

    private static ListMultimap<SchemaPath, YangInstanceIdentifier> decomposeIdentifiers(
            final Set<DOMRpcIdentifier> rpcs) {
        final ListMultimap<SchemaPath, YangInstanceIdentifier> ret = LinkedListMultimap.create();
        for (DOMRpcIdentifier i : rpcs) {
            ret.put(i.getType(), i.getContextReference());
        }
        return ret;
    }

    DOMRpcRoutingTable add(final DOMOperationImplementation implementation, final Set<DOMRpcIdentifier> rpcsToAdd) {
        if (rpcsToAdd.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, YangInstanceIdentifier> toAdd = decomposeIdentifiers(rpcsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, AbstractDOMOperationRoutingTableEntry> mb = ImmutableMap.builder();
        for (Entry<SchemaPath, AbstractDOMOperationRoutingTableEntry> re : this.rpcs.entrySet()) {
            List<YangInstanceIdentifier> newRpcs = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newRpcs.isEmpty()) {
                final AbstractDOMOperationRoutingTableEntry ne = re.getValue().add(implementation, newRpcs);
                mb.put(re.getKey(), ne);
            } else {
                mb.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<SchemaPath, Collection<YangInstanceIdentifier>> e : toAdd.asMap().entrySet()) {
            final Builder<YangInstanceIdentifier, List<DOMOperationImplementation>> vb = ImmutableMap.builder();
            final List<DOMOperationImplementation> v = ImmutableList.of(implementation);
            for (YangInstanceIdentifier i : e.getValue()) {
                vb.put(i, v);
            }

            mb.put(e.getKey(), createOperationEntry(schemaContext, e.getKey(), vb.build()));
        }

        return new DOMRpcRoutingTable(mb.build(), schemaContext);
    }

    DOMRpcRoutingTable remove(final DOMOperationImplementation implementation, final Set<DOMRpcIdentifier> rpcIds) {
        if (rpcIds.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, YangInstanceIdentifier> toRemove = decomposeIdentifiers(rpcIds);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, AbstractDOMOperationRoutingTableEntry> b = ImmutableMap.builder();
        for (Entry<SchemaPath, AbstractDOMOperationRoutingTableEntry> e : this.rpcs.entrySet()) {
            final List<YangInstanceIdentifier> removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final AbstractDOMOperationRoutingTableEntry ne = e.getValue().remove(implementation, removed);
                if (ne != null) {
                    b.put(e.getKey(), ne);
                }
            } else {
                b.put(e);
            }
        }

        // All done, whatever is in toRemove, was not there in the first place
        return new DOMRpcRoutingTable(b.build(), schemaContext);
    }

    @VisibleForTesting
    Map<SchemaPath, Set<YangInstanceIdentifier>> getRpcs() {
        return Maps.transformValues(rpcs, AbstractDOMOperationRoutingTableEntry::registeredIdentifiers);
    }

    Map<SchemaPath, Set<YangInstanceIdentifier>> getRpcs(final DOMRpcAvailabilityListener listener) {
        final Map<SchemaPath, Set<YangInstanceIdentifier>> ret = new HashMap<>(rpcs.size());
        for (Entry<SchemaPath, AbstractDOMOperationRoutingTableEntry> e : rpcs.entrySet()) {
            final Set<YangInstanceIdentifier> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    Map<SchemaPath, Set<YangInstanceIdentifier>> getOperations(final DOMOperationAvailabilityListener listener) {
        final Map<SchemaPath, Set<YangInstanceIdentifier>> ret = new HashMap<>(rpcs.size());
        for (Entry<SchemaPath, AbstractDOMOperationRoutingTableEntry> e : rpcs.entrySet()) {
            final Set<YangInstanceIdentifier> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    private static OperationDefinition findOperationDefination(final SchemaContext context,
            final SchemaPath schemaPath) {
        if (context != null) {
            final int size = Iterables.size(schemaPath.getPathFromRoot()) - 1;
            //Rpc
            if (size <= 0) {
                final QName qname = schemaPath.getPathFromRoot().iterator().next();
                final Module module = context.findModule(qname.getModule()).orElse(null);
                if (module != null && module.getRpcs() != null) {
                    for (RpcDefinition rpc : module.getRpcs()) {
                        if (qname.equals(rpc.getQName())) {
                            return rpc;
                        }
                    }
                }
            } else {
                //Action
                final SchemaNode node = SchemaContextUtil.findDataSchemaNode(context, schemaPath.getParent());
                if (node != null) {
                    if (node instanceof ActionNodeContainer) {
                        for (ActionDefinition action : ((ActionNodeContainer) node).getActions()) {
                            if (action.getQName().equals(schemaPath.getLastComponent())) {
                                return action;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static AbstractDOMOperationRoutingTableEntry createOperationEntry(final SchemaContext context,
            final SchemaPath key, final Map<YangInstanceIdentifier,
            List<DOMOperationImplementation>> implementations) {
        final OperationDefinition oprDef = findOperationDefination(context, key);

        if (oprDef == null) {
            return new UnknownDOMRpcRoutingTableEntry(key, implementations);
        }

        if (oprDef instanceof ActionDefinition) {
            return new DOMActionRoutingTableEntry(key, implementations);
        } else {
            final RpcRoutingStrategy strategy = RpcRoutingStrategy.from((RpcDefinition) oprDef);
            if (strategy.isContextBasedRouted()) {
                return new RoutedDOMRpcRoutingTableEntry((RpcDefinition) oprDef,
                    YangInstanceIdentifier.of(strategy.getLeaf()), implementations);
            }

            return new GlobalDOMRpcRoutingTableEntry((RpcDefinition) oprDef, implementations);
        }
    }

    public void invokeRpc(@Nonnull SchemaPath type, @Nullable NormalizedNode<?, ?> input,
            BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        final AbstractDOMOperationRoutingTableEntry entry = rpcs.get(type);
        if (entry == null || !(entry instanceof AbstractDOMRpcRoutingTableEntry)) {
            callback.accept(new DefaultDOMRpcResult(),
                new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", type));
            return;
        }

        ((AbstractDOMRpcRoutingTableEntry) entry).invokeRpc(input, callback);
    }

    void invokeAction(@Nonnull SchemaPath type, @Nonnull YangInstanceIdentifier parent,
            @Nullable NormalizedNode<?, ?> input, BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        final AbstractDOMOperationRoutingTableEntry entry = rpcs.get(type);
        if (entry == null || !(entry instanceof DOMActionRoutingTableEntry)) {
            callback.accept(new DefaultDOMRpcResult(),
                new DOMRpcImplementationNotAvailableException("No implementation of RPC %s available", type));
        }
        Preconditions.checkArgument(entry instanceof DOMActionRoutingTableEntry);
        ((DOMActionRoutingTableEntry) entry).invokeAction(parent, input, callback);
    }

    DOMRpcRoutingTable setSchemaContext(final SchemaContext context) {
        final Builder<SchemaPath, AbstractDOMOperationRoutingTableEntry> b = ImmutableMap.builder();

        for (Entry<SchemaPath, AbstractDOMOperationRoutingTableEntry> e : rpcs.entrySet()) {
            b.put(e.getKey(), createOperationEntry(context, e.getKey(), e.getValue().getImplementations()));
        }

        return new DOMRpcRoutingTable(b.build(), context);
    }
}
