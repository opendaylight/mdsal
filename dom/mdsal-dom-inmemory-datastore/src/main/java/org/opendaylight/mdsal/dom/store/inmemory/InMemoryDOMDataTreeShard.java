/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.DOMDataTreePrefixTable;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

@Beta
public class InMemoryDOMDataTreeShard implements ReadableWriteableDOMDataTreeShard, SchemaContextListener {

    private static final class SubshardProducerSpecification {
        private final Collection<DOMDataTreeIdentifier> prefixes = new ArrayList<>(1);
        private final ChildShardContext shard;

        SubshardProducerSpecification(final ChildShardContext subshard) {
            this.shard = Preconditions.checkNotNull(subshard);
        }

        void addPrefix(final DOMDataTreeIdentifier prefix) {
            prefixes.add(prefix);
        }

        DOMDataTreeShardProducer createProducer() {
            return shard.getShard().createProducer(prefixes);
        }

        public DOMDataTreeIdentifier getPrefix() {
            return shard.getPrefix();
        }
    }

    private final DOMDataTreePrefixTable<ChildShardContext> childShardsTable = DOMDataTreePrefixTable.create();
    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards = new HashMap<>();
    private final DOMDataTreeIdentifier prefix;
    private final DataTree dataTree;
    private final InMemoryDOMDataTreeShardChangePublisher shardChangePublisher;
    private final ListeningExecutorService executor;

    private InMemoryDOMDataTreeShard(final DOMDataTreeIdentifier prefix, final ExecutorService dataTreeChangeExecutor,
                                     final int maxDataChangeListenerQueueSize) {
        this.prefix = Preconditions.checkNotNull(prefix);

        final TreeType treeType = treeTypeFor(prefix.getDatastoreType());
        this.dataTree = InMemoryDataTreeFactory.getInstance().create(treeType, prefix.getRootIdentifier());

        this.shardChangePublisher = new InMemoryDOMDataTreeShardChangePublisher(dataTreeChangeExecutor,
                maxDataChangeListenerQueueSize, dataTree, prefix.getRootIdentifier(), childShards);
        this.executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }

    public static InMemoryDOMDataTreeShard create(final DOMDataTreeIdentifier id,
            final ExecutorService dataTreeChangeExecutor, final int maxDataChangeListenerQueueSize) {
        return new InMemoryDOMDataTreeShard(id, dataTreeChangeExecutor, maxDataChangeListenerQueueSize);
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        dataTree.setSchemaContext(context);
    }

    @Override
    public void onChildAttached(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        Preconditions.checkArgument(child != this, "Attempted to attach child %s onto self", this);
        reparentChildShards(prefix, child);
        addChildShard(prefix, child);
    }

    @Override
    public void onChildDetached(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        childShards.remove(prefix);
        childShardsTable.remove(prefix);
    }

    @Override
    public InMemoryDOMDataTreeShardProducer createProducer(final Collection<DOMDataTreeIdentifier> prefixes) {
        for (final DOMDataTreeIdentifier prodPrefix : prefixes) {
            Preconditions.checkArgument(prefix.contains(prodPrefix), "Prefix %s is not contained under shart root",
                prodPrefix, prefix);
        }
        return new InMemoryDOMDataTreeShardProducer(this, prefixes);
    }

    @Nonnull
    @Override
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerTreeChangeListener(
            @Nonnull final YangInstanceIdentifier treeId, @Nonnull final L listener) {
        return shardChangePublisher.registerTreeChangeListener(treeId, listener);
    }

    private void addChildShard(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        final ChildShardContext context = createContextFor(prefix, child);
        childShards.put(prefix, context);
        childShardsTable.store(prefix, context);
    }

    private void reparentChildShards(final DOMDataTreeIdentifier newChildPrefix, final DOMDataTreeShard newChild) {
        final Iterator<Entry<DOMDataTreeIdentifier, ChildShardContext>> actualChildren =
                childShards.entrySet().iterator();
        final Map<DOMDataTreeIdentifier, ChildShardContext> reparented = new HashMap<>();
        while (actualChildren.hasNext()) {
            final Entry<DOMDataTreeIdentifier, ChildShardContext> actualChild = actualChildren.next();
            final DOMDataTreeIdentifier actualPrefix = actualChild.getKey();
            Preconditions.checkArgument(!newChildPrefix.equals(actualPrefix),
                    "Child shard with prefix %s already attached", newChildPrefix);
            if (newChildPrefix.contains(actualPrefix)) {
                final ChildShardContext actualContext = actualChild.getValue();
                actualChildren.remove();
                newChild.onChildAttached(actualPrefix, actualContext.getShard());
                reparented.put(actualChild.getKey(), actualContext);
                childShardsTable.remove(actualPrefix);
            }
        }
        updateProducersAndListeners(reparented);
    }

    private void updateProducersAndListeners(final Map<DOMDataTreeIdentifier, ChildShardContext> reparented) {
        // FIXME: remove reparenting of producers, shards have to be registered from top to bottom
        if (reparented.isEmpty()) {
            //nothing was reparented no need to update anything
            return;
        }
        throw new UnsupportedOperationException();
    }

    private static ChildShardContext createContextFor(final DOMDataTreeIdentifier prefix,
            final DOMDataTreeShard child) {
        Preconditions.checkArgument(child instanceof WriteableDOMDataTreeShard,
            "Child %s is not a writable shared", child);
        return new ChildShardContext(prefix, (WriteableDOMDataTreeShard) child);
    }

    private static TreeType treeTypeFor(final LogicalDatastoreType dsType) {
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
        final Map<DOMDataTreeIdentifier, DOMDataTreeShard> ret = new HashMap<>();
        for (final Entry<DOMDataTreeIdentifier, ChildShardContext> entry : childShards.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getShard());
        }
        return ret;
    }

    InmemoryDOMDataTreeShardWriteTransaction createTransaction(
            final InmemoryDOMDataTreeShardWriteTransaction previousTx) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    InmemoryDOMDataTreeShardWriteTransaction createTransaction(final Collection<DOMDataTreeIdentifier> prefixes) {

        final Map<DOMDataTreeIdentifier, SubshardProducerSpecification> affectedSubshards = new HashMap<>();
        for (final DOMDataTreeIdentifier producerPrefix : prefixes) {
            for (final ChildShardContext maybeAffected : childShards.values()) {
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

        final ShardRootModificationContext rootContext = new ShardRootModificationContext(prefix,
                (CursorAwareDataTreeSnapshot) dataTree.takeSnapshot());
        final ShardDataModificationBuilder builder = new ShardDataModificationBuilder(rootContext);
        for (final SubshardProducerSpecification spec : affectedSubshards.values()) {
            final ForeignShardModificationContext foreignContext =
                    new ForeignShardModificationContext(spec.getPrefix(), spec.createProducer());
            builder.addSubshard(foreignContext);
            builder.addSubshard(spec.getPrefix(), foreignContext);
        }

        return new InmemoryDOMDataTreeShardWriteTransaction(builder.build(), dataTree, shardChangePublisher, executor);
    }
}
