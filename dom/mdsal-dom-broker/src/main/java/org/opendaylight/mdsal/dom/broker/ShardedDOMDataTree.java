/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingConflictException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardingService;
import org.opendaylight.mdsal.dom.spi.DOMDataTreePrefixTable;
import org.opendaylight.mdsal.dom.spi.DOMDataTreePrefixTableEntry;
import org.opendaylight.mdsal.dom.spi.shard.ListenableDOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShardedDOMDataTree implements DOMDataTreeService, DOMDataTreeShardingService {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTree.class);

    private static final AtomicLong COUNTER = new AtomicLong();
    private static final int MAX_AGGREGATOR_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_AGGREGATOR_QUEUE = 2000;
    private static final int MAX_LISTENER_QUEUE = 2000;

    @GuardedBy("this")
    private final DOMDataTreePrefixTable<DOMDataTreeShardRegistration<?>> shards = DOMDataTreePrefixTable.create();
    @GuardedBy("this")
    private final DOMDataTreePrefixTable<DOMDataTreeProducer> producers = DOMDataTreePrefixTable.create();

//    private final QueuedNotificationManager<DOMDataTreeListener, DataTreeCandidate> aggregatorQueue;
//    private final ExecutorService aggregatorExecutor;

//    public ShardedDOMDataTree() {
//        final String prefix = "dtcl-aggregator-" + COUNTER.getAndIncrement();
//        final FastThreadPoolExecutor executor =
//                new FastThreadPoolExecutor(MAX_AGGREGATOR_THREADS, MAX_AGGREGATOR_QUEUE, prefix);
//        executor.setRejectedExecutionHandler(ExecutorServiceUtil.waitInQueueExecutionHandler());
//
//        aggregatorExecutor = executor;
//        aggregatorQueue = QueuedNotificationManager.create(executor, listenerInvoker, MAX_LISTENER_QUEUE, prefix);
//    }

    void removeShard(final DOMDataTreeShardRegistration<?> reg) {
        final DOMDataTreeIdentifier prefix = reg.getPrefix();
        final DOMDataTreeShardRegistration<?> parentReg;

        synchronized (this) {
            shards.remove(prefix);
            parentReg = shards.lookup(prefix).getValue();

            /*
             * FIXME: adjust all producers and listeners. This is tricky, as we need different
             * locking strategy, simply because we risk AB/BA deadlock with a producer being split
             * off from a producer.
             */
        }

        if (parentReg != null) {
            parentReg.getInstance().onChildDetached(prefix, reg.getInstance());
        }
    }

    @Override
    public <T extends DOMDataTreeShard> ListenerRegistration<T> registerDataTreeShard(
            final DOMDataTreeIdentifier prefix, final T shard, final DOMDataTreeProducer producer)
                    throws DOMDataTreeShardingConflictException {

        final DOMDataTreeIdentifier firstSubtree = Iterables.getOnlyElement(((
                ShardedDOMDataTreeProducer) producer).getSubtrees());
        Preconditions.checkArgument(firstSubtree != null, "Producer that is used to verify namespace claim can"
                + " only claim a single namespace");
        Preconditions.checkArgument(prefix.equals(firstSubtree), "Trying to register shard to a different namespace"
                + " than the producer has claimed");

        final DOMDataTreeShardRegistration<T> reg;
        final DOMDataTreeShardRegistration<?> parentReg;

        synchronized (this) {
            /*
             * Lookup the parent shard (e.g. the one which currently matches the prefix),
             * and if it exists, check if its registration prefix does not collide with
             * this registration.
             */
            final DOMDataTreePrefixTableEntry<DOMDataTreeShardRegistration<?>> parent = shards.lookup(prefix);
            if (parent != null) {
                parentReg = parent.getValue();
                if (parentReg != null && prefix.equals(parentReg.getPrefix())) {
                    throw new DOMDataTreeShardingConflictException(String.format(
                            "Prefix %s is already occupied by shard %s", prefix, parentReg.getInstance()));
                }
            } else {
                parentReg = null;
            }

            // FIXME: wrap the shard in a proper adaptor based on implemented interface

            reg = new DOMDataTreeShardRegistration<>(this, prefix, shard);

            shards.store(prefix, reg);

            ((ShardedDOMDataTreeProducer) producer).subshardAdded(Collections.singletonMap(prefix, shard));
        }

        // Notify the parent shard
        if (parentReg != null) {
            parentReg.getInstance().onChildAttached(prefix, shard);
        }

        return reg;
    }

    @GuardedBy("this")
    private DOMDataTreeProducer findProducer(final DOMDataTreeIdentifier subtree) {

        final DOMDataTreePrefixTableEntry<DOMDataTreeProducer> producerEntry = producers.lookup(subtree);
        if (producerEntry != null) {
            return producerEntry.getValue();
        }
        return null;
    }

    synchronized void destroyProducer(final ShardedDOMDataTreeProducer producer) {
        for (final DOMDataTreeIdentifier s : producer.getSubtrees()) {
            producers.remove(s);
        }
    }

    @GuardedBy("this")
    private DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees,
            final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        // Record the producer's attachment points
        final DOMDataTreeProducer ret = ShardedDOMDataTreeProducer.create(this, subtrees, shardMap);
        for (final DOMDataTreeIdentifier subtree : subtrees) {
            producers.store(subtree, ret);
        }

        return ret;
    }

    @Override
    public synchronized DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkArgument(!subtrees.isEmpty(), "Subtrees may not be empty");

        final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap = new HashMap<>();
        for (final DOMDataTreeIdentifier subtree : subtrees) {
            // Attempting to create a disconnected producer -- all subtrees have to be unclaimed
            final DOMDataTreeProducer producer = findProducer(subtree);
            Preconditions.checkArgument(producer == null, "Subtree %s is attached to producer %s", subtree, producer);

            final DOMDataTreePrefixTableEntry<DOMDataTreeShardRegistration<?>> possibleShardReg =
                    shards.lookup(subtree);
            if (possibleShardReg != null && possibleShardReg.getValue() != null) {
                shardMap.put(subtree, possibleShardReg.getValue().getInstance());
            }
        }

        return createProducer(subtrees, shardMap);
    }

    synchronized DOMDataTreeProducer createProducer(final ShardedDOMDataTreeProducer parent,
            final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkNotNull(parent);

        final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap = new HashMap<>();
        for (final DOMDataTreeIdentifier s : subtrees) {
            shardMap.put(s, shards.lookup(s).getValue().getInstance());
        }

        return createProducer(subtrees, shardMap);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public synchronized <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges,
            final Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException {
        Preconditions.checkNotNull(listener, "listener");

        // Cross-check specified trees for exclusivity and eliminate duplicates, noDupSubtrees is effectively a Set
        final Collection<DOMDataTreeIdentifier> noDupSubtrees;
        switch (subtrees.size()) {
            case 0:
                throw new IllegalArgumentException("Subtrees must not be empty.");
            case 1:
                noDupSubtrees = subtrees;
                break;
            default:
                // Check subtrees for mutual inclusion, this is an O(N**2) operation
                for (DOMDataTreeIdentifier toCheck : subtrees) {
                    for (DOMDataTreeIdentifier against : subtrees) {
                        if (!toCheck.equals(against)) {
                            Preconditions.checkArgument(!toCheck.contains(against), "Subtree %s contains subtree %s",
                                toCheck, against);
                        }
                    }
                }

                noDupSubtrees = ImmutableSet.copyOf(subtrees);
        }

        LOG.trace("Requested registration of listener {} to subtrees {}", listener, noDupSubtrees);

        // Lookup shards corresponding to subtrees and construct a map of which subtrees we want from which shard
        final ListMultimap<DOMDataTreeShardRegistration<?>, DOMDataTreeIdentifier> needed =
                ArrayListMultimap.create();
        for (final DOMDataTreeIdentifier subtree : subtrees) {
            final DOMDataTreeShardRegistration<?> reg = Verify.verifyNotNull(shards.lookup(subtree).getValue());
            needed.put(reg, subtree);
        }

        LOG.trace("Listener {} is attaching to shards {}", listener, needed);

        // Sanity check: all selected shards have to support one of the listening interfaces
        needed.asMap().forEach((reg, trees) -> {
            final DOMDataTreeShard shard = reg.getInstance();
            Preconditions.checkArgument(shard instanceof ListenableDOMDataTreeShard
                || shard instanceof DOMStoreTreeChangePublisher, "Subtrees %s do not point to listenable subtree.",
                trees);
        });

        // Sanity check: all producers have to come from this implementation and must not form loops
        for (DOMDataTreeProducer producer : producers) {
            Preconditions.checkArgument(producer instanceof ShardedDOMDataTreeProducer);
            simpleLoopCheck(subtrees, ((ShardedDOMDataTreeProducer) producer).getSubtrees());
        }

        final ListenerRegistration<?> underlyingRegistration = createRegisteredListener(listener, needed.asMap(),
            allowRxMerges, producers);
        return new AbstractListenerRegistration<T>(listener) {
            @Override
            protected void removeRegistration() {
                ShardedDOMDataTree.this.removeListener(listener);
                underlyingRegistration.close();
            }
        };
    }

    private ListenerRegistration<?> createRegisteredListener(final DOMDataTreeListener userListener,
            final Map<DOMDataTreeShardRegistration<?>, Collection<DOMDataTreeIdentifier>> needed,
            final boolean allowRxMerges, final Collection<DOMDataTreeProducer> producers) {

        // Separate out the non-aggregator case, where we do not have to do any work (on the high level)
        if (needed.size() == 1) {
            // FIXME: Add attachment of producers
            for (final DOMDataTreeProducer producer : producers) {
                // FIXME: We should also unbound listeners
                ((ShardedDOMDataTreeProducer) producer).bindToListener(userListener);
            }

            final Entry<DOMDataTreeShardRegistration<?>, Collection<DOMDataTreeIdentifier>> e = needed.entrySet()
                    .iterator().next();
            return ensureListenableShard(e.getKey().getInstance()).registerListener(userListener, e.getValue(),
                allowRxMerges);
        }

        // Create an aggregator and start it.
        final Collection<ListenerRegistration<?>> regs = new ArrayList<>(needed.size());
        final DOMDataTreeListenerAggregator aggregator = new DOMDataTreeListenerAggregator(allowRxMerges);

        for (Entry<DOMDataTreeShardRegistration<?>, Collection<DOMDataTreeIdentifier>> e : needed.entrySet()) {
            regs.add(ensureListenableShard(e.getKey().getInstance()).registerListener(aggregator.createListener(treeId),
                e.getValue(), allowRxMerges));
        }

        aggregator.start(userListener);
        return new AbstractListenerRegistration<DOMDataTreeListener>(userListener) {
            @Override
            protected void removeRegistration() {
                regs.forEach(ListenerRegistration::close);
            }
        };
    }

    private static ListenableDOMDataTreeShard ensureListenableShard(final DOMDataTreeShard shard) {
        return shard instanceof ListenableDOMDataTreeShard ? (ListenableDOMDataTreeShard) shard
                : new CompatListenableDOMDataTreeShard(shard);
    }

    private static void simpleLoopCheck(final Collection<DOMDataTreeIdentifier> listen,
            final Set<DOMDataTreeIdentifier> writes) throws DOMDataTreeLoopException {
        for (final DOMDataTreeIdentifier listenPath : listen) {
            for (final DOMDataTreeIdentifier writePath : writes) {
                if (listenPath.contains(writePath)) {
                    throw new DOMDataTreeLoopException(String.format(
                            "Listener must not listen on parent (%s), and also writes child (%s)", listenPath,
                            writePath));
                } else if (writePath.contains(listenPath)) {
                    throw new DOMDataTreeLoopException(
                            String.format("Listener must not write parent (%s), and also listen on child (%s)",
                                    writePath, listenPath));
                }
            }
        }
    }

    void removeListener(final DOMDataTreeListener listener) {
        // FIXME: detach producers
    }
}
