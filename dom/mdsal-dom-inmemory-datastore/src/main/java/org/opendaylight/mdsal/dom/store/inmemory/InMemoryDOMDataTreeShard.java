/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.DOMDataTreePrefixTable;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

public class InMemoryDOMDataTreeShard implements WriteableDOMDataTreeShard, SchemaContextListener {

    private class SubshardProducerSpecification {

        private final ChildShardContext shard;
        private final Collection<DOMDataTreeIdentifier> prefixes = new ArrayList<>();

        SubshardProducerSpecification(ChildShardContext subshard) {
            this.shard = Preconditions.checkNotNull(subshard);
        }

        void addPrefix(DOMDataTreeIdentifier prefix) {
            prefixes.add(prefix);
        }

        DOMDataTreeShardProducer createProducer() {
            return shard.getShard().createProducer(prefixes);
        }

        public DOMDataTreeIdentifier getPrefix() {
            return shard.getPrefix();
        }

    }

    private class ChildShardContext {

        private final WriteableDOMDataTreeShard shard;
        private DOMDataTreeIdentifier prefix;

        public ChildShardContext(DOMDataTreeIdentifier prefix, WriteableDOMDataTreeShard shard) {
            this.prefix = prefix;
            this.shard = shard;
        }

        public WriteableDOMDataTreeShard getShard() {
            return shard;
        }

        public DOMDataTreeIdentifier getPrefix() {
            return prefix;
        }

    }

    Map<DOMDataTreeIdentifier, ChildShardContext> childShards = new HashMap<>();
    DOMDataTreePrefixTable<ChildShardContext> childShardsTable = DOMDataTreePrefixTable.create();

    private final DOMDataTreeIdentifier prefix;
    private final DataTree dataTree;

    public InMemoryDOMDataTreeShard(DOMDataTreeIdentifier prefix) {
        this.prefix = Preconditions.checkNotNull(prefix);
        TreeType treeType = treeTypeFor(prefix.getDatastoreType());
        this.dataTree = prefix.getRootIdentifier().isEmpty() ? InMemoryDataTreeFactory.getInstance().create(treeType)
                : InMemoryDataTreeFactory.getInstance().create(treeType, prefix.getRootIdentifier());
    }

    public static InMemoryDOMDataTreeShard create(DOMDataTreeIdentifier id) {
        return new InMemoryDOMDataTreeShard(id);
    }

    @Override
    public void onGlobalContextUpdated(SchemaContext context) {
        dataTree.setSchemaContext(context);
    }

    @Override
    public void onChildAttached(DOMDataTreeIdentifier prefix, DOMDataTreeShard child) {
        Preconditions.checkArgument(child != this);
        reparentChildShards(prefix, child);
        addChildShard(prefix, child);

    }

    @Override
    public void onChildDetached(DOMDataTreeIdentifier prefix, DOMDataTreeShard child) {
        childShards.remove(prefix);
    }

    @Override
    public InMemoryDOMDataTreeShardProducer createProducer(Collection<DOMDataTreeIdentifier> prefixes) {
        for (DOMDataTreeIdentifier prodPrefix : prefixes) {
            Preconditions.checkArgument(prefix.contains(prodPrefix));
        }
        return new InMemoryDOMDataTreeShardProducer(this, prefixes);
    }

    private void addChildShard(DOMDataTreeIdentifier prefix, DOMDataTreeShard child) {
        ChildShardContext context = createContextFor(prefix, child);
        childShards.put(prefix, context);
        childShardsTable.store(prefix, context);
    }

    private void reparentChildShards(DOMDataTreeIdentifier newChildPrefix, DOMDataTreeShard newChild) {
        Iterator<Entry<DOMDataTreeIdentifier, ChildShardContext>> actualChildren = childShards.entrySet().iterator();
        Map<DOMDataTreeIdentifier, ChildShardContext> reparented = new HashMap<>();
        while (actualChildren.hasNext()) {
            final Entry<DOMDataTreeIdentifier, ChildShardContext> actualChild = actualChildren.next();
            final DOMDataTreeIdentifier actualPrefix = actualChild.getKey();
            Preconditions.checkArgument(!newChildPrefix.equals(actualPrefix),
                    "Child shard with same prefix already attached");
            if (newChildPrefix.contains(actualPrefix)) {
                ChildShardContext actualContext = actualChild.getValue();
                actualChildren.remove();
                newChild.onChildAttached(actualPrefix, actualContext.getShard());
                reparented.put(actualChild.getKey(), actualContext);
                childShardsTable.remove(actualPrefix);
            }
        }
        updateProducersAndListeners(reparented);
    }

    private void updateProducersAndListeners(Map<DOMDataTreeIdentifier, ChildShardContext> reparented) {
        // TODO Auto-generated method stub

    }



    private ChildShardContext createContextFor(DOMDataTreeIdentifier prefix, DOMDataTreeShard child) {
        Preconditions.checkArgument(child instanceof WriteableDOMDataTreeShard);
        return new ChildShardContext(prefix, (WriteableDOMDataTreeShard) child);
    }

    private static TreeType treeTypeFor(LogicalDatastoreType dsType) {
        switch (dsType) {
            case CONFIGURATION:
                return TreeType.CONFIGURATION;
            case OPERATIONAL:
                return TreeType.OPERATIONAL;
            default:
                throw new IllegalArgumentException("Unsupported Data Store type:" + dsType);
        }
    }

    @VisibleForTesting
    Map<DOMDataTreeIdentifier, DOMDataTreeShard> getChildShards() {
        Map<DOMDataTreeIdentifier, DOMDataTreeShard> ret = new HashMap<>();
        for (Entry<DOMDataTreeIdentifier, ChildShardContext> entry : childShards.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getShard());
        }
        return ret;
    }

    InmemoryDOMDataTreeShardWriteTransaction createTransaction(InmemoryDOMDataTreeShardWriteTransaction previousTx) {
        return null;
    }

    InmemoryDOMDataTreeShardWriteTransaction createTransaction(Collection<DOMDataTreeIdentifier> prefixes) {

        Map<DOMDataTreeIdentifier, SubshardProducerSpecification> affectedSubshards = new HashMap<>();
        for (DOMDataTreeIdentifier producerPrefix : prefixes) {
            for (ChildShardContext maybeAffected : childShards.values()) {
                final DOMDataTreeIdentifier bindPath;
                if (producerPrefix.contains(maybeAffected.getPrefix())) {
                    bindPath = maybeAffected.getPrefix();
                } else if (maybeAffected.getPrefix().contains(producerPrefix)) {
                    // Bound path is inside subshard
                    bindPath = producerPrefix;
                } else {
                    continue;
                }
                SubshardProducerSpecification spec = affectedSubshards.get(maybeAffected.getPrefix());
                if (spec == null) {
                    spec = new SubshardProducerSpecification(maybeAffected);
                    affectedSubshards.put(maybeAffected.getPrefix(), spec);
                }
                spec.addPrefix(bindPath);
            }
        }

        ShardRootModificationContext rootContext = new ShardRootModificationContext(prefix,
                (CursorAwareDataTreeSnapshot) dataTree.takeSnapshot());
        ShardDataModificationBuilder builder = new ShardDataModificationBuilder(rootContext);
        for (SubshardProducerSpecification spec : affectedSubshards.values()) {
            ForeignShardModificationContext foreignContext =
                    new ForeignShardModificationContext(spec.getPrefix(), spec.createProducer());
            builder.addSubshard(foreignContext);
        }
        ShardDataModification root = builder.build();
        return new InmemoryDOMDataTreeShardWriteTransaction(root);
    }

}
