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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;


/**
 * Definition of Action routing table.
 */
@Beta
@NonNullByDefault
final class DOMActionRoutingTable {
    static final DOMActionRoutingTable EMPTY = new DOMActionRoutingTable(ImmutableMap.of(), null);

    private final Map<SchemaPath, DOMActionRoutingTableEntry> actions;
    private final SchemaContext schemaContext;

    private DOMActionRoutingTable(final Map<SchemaPath, DOMActionRoutingTableEntry> actions,
                                  final SchemaContext schemaContext) {
        this.actions = Preconditions.checkNotNull(actions);
        this.schemaContext = schemaContext;
    }

    private ListMultimap<SchemaPath, DOMDataTreeIdentifier> decomposeInstances(final Set<DOMActionInstance> instances) {
        final ListMultimap<SchemaPath, DOMDataTreeIdentifier> ret = LinkedListMultimap.create();
        for (DOMActionInstance instance : instances) {
            instance.getDataTrees().forEach(id -> ret.put(instance.getType(), id));
        }
        return ret;
    }


    private static DOMActionRoutingTableEntry createActionEntry(final SchemaContext context, final SchemaPath type,
            final Map<DOMDataTreeIdentifier, List<DOMActionImplementation>> implementations) {
        final ActionDefinition actionDef = findActionDefinition(context, type);
        if (actionDef == null) {
            //FIXME: return null directly instead of providing kind of unknown entry.
            return null;
        }

        return new DOMActionRoutingTableEntry(type, implementations);
    }


    DOMActionRoutingTable setSchemaContext(final SchemaContext context) {
        final Builder<SchemaPath, DOMActionRoutingTableEntry> b = ImmutableMap.builder();

        for (Entry<SchemaPath, DOMActionRoutingTableEntry> e : actions.entrySet()) {
            DOMActionRoutingTableEntry entry =
                createActionEntry(context, e.getKey(), e.getValue().getImplementations());
            if (entry != null) {
                b.put(e.getKey(), entry);
            }
        }

        return new DOMActionRoutingTable(b.build(), context);
    }

    DOMActionRoutingTable add(final DOMActionImplementation implementation, final Set<DOMActionInstance> oprsToAdd) {
        if (oprsToAdd.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, DOMDataTreeIdentifier> toAdd = decomposeInstances(oprsToAdd);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, DOMActionRoutingTableEntry> mb = ImmutableMap.builder();
        for (Entry<SchemaPath, DOMActionRoutingTableEntry> re : this.actions.entrySet()) {
            List<DOMDataTreeIdentifier> newOprs = new ArrayList<>(toAdd.removeAll(re.getKey()));
            if (!newOprs.isEmpty()) {
                final DOMActionRoutingTableEntry ne = re.getValue().add(implementation, newOprs);
                mb.put(re.getKey(), ne);
            } else {
                mb.put(re);
            }
        }

        // Finally add whatever is left in the decomposed multimap
        for (Entry<SchemaPath, Collection<DOMDataTreeIdentifier>> e : toAdd.asMap().entrySet()) {
            final Builder<DOMDataTreeIdentifier, List<DOMActionImplementation>> vb = ImmutableMap.builder();
            final List<DOMActionImplementation> v = ImmutableList.of(implementation);
            for (DOMDataTreeIdentifier i : e.getValue()) {
                vb.put(i, v);
            }
            DOMActionRoutingTableEntry entry = createActionEntry(schemaContext, e.getKey(), vb.build());
            if (entry != null) {
                mb.put(e.getKey(), entry);
            }
        }

        return new DOMActionRoutingTable(mb.build(), schemaContext);
    }

    DOMActionRoutingTable remove(final DOMActionImplementation implementation, final Set<DOMActionInstance> instances) {
        if (instances.isEmpty()) {
            return this;
        }

        // First decompose the identifiers to a multimap
        final ListMultimap<SchemaPath, DOMDataTreeIdentifier> toRemove = decomposeInstances(instances);

        // Now iterate over existing entries, modifying them as appropriate...
        final Builder<SchemaPath, DOMActionRoutingTableEntry> b = ImmutableMap.builder();
        for (Entry<SchemaPath, DOMActionRoutingTableEntry> e : this.actions.entrySet()) {
            final List<DOMDataTreeIdentifier> removed = new ArrayList<>(toRemove.removeAll(e.getKey()));
            if (!removed.isEmpty()) {
                final DOMActionRoutingTableEntry ne = e.getValue().remove(implementation, removed);
                if (ne != null) {
                    b.put(e.getKey(), ne);
                }
            } else {
                b.put(e);
            }
        }

        // All done, whatever is in toRemove, was not there in the first place
        return new DOMActionRoutingTable(b.build(), schemaContext);
    }

    @VisibleForTesting
    Map<SchemaPath, Set<DOMDataTreeIdentifier>> getActions() {
        return Maps.transformValues(actions, DOMActionRoutingTableEntry::registeredIdentifiers);
    }

    Map<SchemaPath, Set<DOMDataTreeIdentifier>> getActions(final AvailabilityListener listener) {
        final Map<SchemaPath, Set<DOMDataTreeIdentifier>> ret = new HashMap<>(actions.size());
        for (Entry<SchemaPath, DOMActionRoutingTableEntry> e : actions.entrySet()) {
            final Set<DOMDataTreeIdentifier> ids = e.getValue().registeredIdentifiers(listener);
            if (!ids.isEmpty()) {
                ret.put(e.getKey(), ids);
            }
        }

        return ret;
    }

    @Nullable DOMActionRoutingTableEntry getEntry(final @NonNull SchemaPath type) {
        return actions.get(type);
    }

    private static ActionDefinition findActionDefinition(final SchemaContext context, final SchemaPath path) {
        final SchemaNode node = SchemaContextUtil.findDataSchemaNode(context, path.getParent());
        if (node != null) {
            if (node instanceof ActionNodeContainer) {
                for (ActionDefinition action : ((ActionNodeContainer) node).getActions()) {
                    if (action.getQName().equals(path.getLastComponent())) {
                        return action;
                    }
                }
            }
        }
        return null;
    }
}
