/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.DOMDataTreePrefixTable;
import org.opendaylight.mdsal.dom.spi.shard.ChildShardContext;
import org.opendaylight.mdsal.dom.spi.shard.ForeignShardModificationContext;
import org.opendaylight.mdsal.dom.spi.shard.ReadableWriteableDOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.shard.SubshardProducerSpecification;
import org.opendaylight.mdsal.dom.spi.shard.WriteableDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.concurrent.CountingRejectedExecutionHandler;
import org.opendaylight.yangtools.util.concurrent.FastThreadPoolExecutor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public class InMemoryDOMDataTreeShard implements ReadableWriteableDOMDataTreeShard, SchemaContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShard.class);
    private static final int DEFAULT_SUBMIT_QUEUE_SIZE = 1000;

    private final DOMDataTreePrefixTable<ChildShardContext> childShardsTable = DOMDataTreePrefixTable.create();
    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards = new HashMap<>();
    private final Collection<InMemoryDOMDataTreeShardProducer> producers = new HashSet<>();
    private final InMemoryDOMDataTreeShardChangePublisher shardChangePublisher;
    private final ListeningExecutorService executor;
    private final DOMDataTreeIdentifier prefix;
    private final DataTree dataTree;

    InMemoryDOMDataTreeShard(final DOMDataTreeIdentifier prefix, final Executor dataTreeChangeExecutor,
            final int maxDataChangeListenerQueueSize, final int submitQueueSize) {
        this.prefix = requireNonNull(prefix);

        final DataTreeConfiguration treeBaseConfig = treeTypeFor(prefix.getDatastoreType());
        final DataTreeConfiguration treeConfig = new DataTreeConfiguration.Builder(treeBaseConfig.getTreeType())
                .setMandatoryNodesValidation(treeBaseConfig.isMandatoryNodesValidationEnabled())
                .setUniqueIndexes(treeBaseConfig.isUniqueIndexEnabled())
                .setRootPath(prefix.getRootIdentifier())
                .build();

        this.dataTree = new InMemoryDataTreeFactory().create(treeConfig);

        this.shardChangePublisher = new InMemoryDOMDataTreeShardChangePublisher(dataTreeChangeExecutor,
                maxDataChangeListenerQueueSize, dataTree, prefix.getRootIdentifier(), childShards);

        final FastThreadPoolExecutor fte = new FastThreadPoolExecutor(1, submitQueueSize, "Shard[" + prefix + "]",
            InMemoryDOMDataTreeShard.class);
        fte.setRejectedExecutionHandler(CountingRejectedExecutionHandler.newCallerWaitsPolicy());
        this.executor = MoreExecutors.listeningDecorator(fte);
    }

    public static InMemoryDOMDataTreeShard create(final DOMDataTreeIdentifier id,
                                                  final Executor dataTreeChangeExecutor,
                                                  final int maxDataChangeListenerQueueSize) {
        return new InMemoryDOMDataTreeShard(id.toOptimized(), dataTreeChangeExecutor,
                maxDataChangeListenerQueueSize, DEFAULT_SUBMIT_QUEUE_SIZE);
    }

    public static InMemoryDOMDataTreeShard create(final DOMDataTreeIdentifier id,
                                                  final Executor dataTreeChangeExecutor,
                                                  final int maxDataChangeListenerQueueSize,
                                                  final int submitQueueSize) {
        return new InMemoryDOMDataTreeShard(id.toOptimized(), dataTreeChangeExecutor,
                maxDataChangeListenerQueueSize, submitQueueSize);
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        dataTree.setSchemaContext(context);
    }

    @Override
    public void onChildAttached(final DOMDataTreeIdentifier childPrefix, final DOMDataTreeShard child) {
        checkArgument(child != this, "Attempted to attach child %s onto self", this);
        reparentChildShards(childPrefix, child);

        final ChildShardContext context = createContextFor(childPrefix, child);
        childShards.put(childPrefix, context);
        childShardsTable.store(childPrefix, context);
        updateProducers();
    }

    @Override
    public void onChildDetached(final DOMDataTreeIdentifier childPrefix, final DOMDataTreeShard child) {
        childShards.remove(childPrefix);
        childShardsTable.remove(childPrefix);
        //FIXME: Producers not being affected could be skipped over.
        updateProducers();
    }

    private void updateProducers() {
        for (InMemoryDOMDataTreeShardProducer p : producers) {
            p.setModificationFactory(createModificationFactory(p.getPrefixes()));
        }
    }

    @VisibleForTesting
    InMemoryShardDataModificationFactory createModificationFactory(final Collection<DOMDataTreeIdentifier> prefixes) {
        final Map<DOMDataTreeIdentifier, SubshardProducerSpecification> affected = new HashMap<>();
        for (final DOMDataTreeIdentifier producerPrefix : prefixes) {
            for (final ChildShardContext child : childShards.values()) {
                final DOMDataTreeIdentifier bindPath;
                if (producerPrefix.contains(child.getPrefix())) {
                    bindPath = child.getPrefix();
                } else if (child.getPrefix().contains(producerPrefix)) {
                    // Bound path is inside subshard
                    bindPath = producerPrefix;
                } else {
                    continue;
                }

                SubshardProducerSpecification spec = affected.get(child.getPrefix());
                if (spec == null) {
                    spec = new SubshardProducerSpecification(child);
                    affected.put(child.getPrefix(), spec);
                }
                spec.addPrefix(bindPath);
            }
        }

        final InmemoryShardDataModificationFactoryBuilder builder =
                new InmemoryShardDataModificationFactoryBuilder(prefix);
        for (final SubshardProducerSpecification spec : affected.values()) {
            final ForeignShardModificationContext foreignContext =
                    new ForeignShardModificationContext(spec.getPrefix(), spec.createProducer());
            builder.addSubshard(foreignContext);
            builder.addSubshard(spec.getPrefix(), foreignContext);
        }

        return builder.build();
    }

    @Override
    public InMemoryDOMDataTreeShardProducer createProducer(final Collection<DOMDataTreeIdentifier> prefixes) {
        for (final DOMDataTreeIdentifier prodPrefix : prefixes) {
            checkArgument(prefix.contains(prodPrefix), "Prefix %s is not contained under shard root", prodPrefix,
                prefix);
        }

        final InMemoryDOMDataTreeShardProducer ret = new InMemoryDOMDataTreeShardProducer(this, prefixes,
                createModificationFactory(prefixes));
        producers.add(ret);
        return ret;
    }

    void closeProducer(final InMemoryDOMDataTreeShardProducer producer) {
        synchronized (this) {
            if (!producers.remove(producer)) {
                LOG.warn("Producer {} not found in shard {}", producer, this);
            }
        }
    }

    @Override
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerTreeChangeListener(
            final YangInstanceIdentifier treeId, final L listener) {
        return shardChangePublisher.registerTreeChangeListener(treeId, listener);
    }

    private void reparentChildShards(final DOMDataTreeIdentifier newChildPrefix, final DOMDataTreeShard newChild) {
        final Iterator<Entry<DOMDataTreeIdentifier, ChildShardContext>> actualChildren =
                childShards.entrySet().iterator();
        final Map<DOMDataTreeIdentifier, ChildShardContext> reparented = new HashMap<>();
        while (actualChildren.hasNext()) {
            final Entry<DOMDataTreeIdentifier, ChildShardContext> actualChild = actualChildren.next();
            final DOMDataTreeIdentifier actualPrefix = actualChild.getKey();
            checkArgument(!newChildPrefix.equals(actualPrefix), "Child shard with prefix %s already attached",
                newChildPrefix);
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
        checkArgument(child instanceof WriteableDOMDataTreeShard, "Child %s is not a writable shared", child);
        return new ChildShardContext(prefix, (WriteableDOMDataTreeShard) child);
    }

    private static DataTreeConfiguration treeTypeFor(final LogicalDatastoreType dsType) {
        switch (dsType) {
            case CONFIGURATION:
                return DataTreeConfiguration.DEFAULT_CONFIGURATION;
            case OPERATIONAL:
                return DataTreeConfiguration.DEFAULT_OPERATIONAL;
            default:
                throw new IllegalArgumentException("Unsupported Data Store type:" + dsType);
        }
    }

    @VisibleForTesting
    Map<DOMDataTreeIdentifier, DOMDataTreeShard> getChildShards() {
        return ImmutableMap.copyOf(Maps.transformValues(childShards, ChildShardContext::getShard));
    }

    DataTreeSnapshot takeSnapshot() {
        return dataTree.takeSnapshot();
    }

    InmemoryDOMDataTreeShardWriteTransaction createTransaction(final String transactionId,
            final InMemoryDOMDataTreeShardProducer producer, final DataTreeSnapshot snapshot) {
        checkArgument(snapshot instanceof CursorAwareDataTreeSnapshot);
        return new InmemoryDOMDataTreeShardWriteTransaction(producer,
                producer.getModificationFactory().createModification((CursorAwareDataTreeSnapshot) snapshot), dataTree,
                shardChangePublisher, executor);
    }

    @VisibleForTesting
    public Collection<InMemoryDOMDataTreeShardProducer> getProducers() {
        return producers;
    }
}
