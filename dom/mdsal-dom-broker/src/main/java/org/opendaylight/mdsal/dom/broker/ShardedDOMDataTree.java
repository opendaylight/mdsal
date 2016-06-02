/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShardedDOMDataTree implements DOMDataTreeService, DOMDataTreeShardingService {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTree.class);

    @GuardedBy("this")
    private final DOMDataTreePrefixTable<ShardRegistration<?>> shards = DOMDataTreePrefixTable.create();
    @GuardedBy("this")
    private final DOMDataTreePrefixTable<DOMDataTreeProducer> producers = DOMDataTreePrefixTable.create();


    void removeShard(final ShardRegistration<?> reg) {
        final DOMDataTreeIdentifier prefix = reg.getPrefix();
        final ShardRegistration<?> parentReg;

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
    public <T extends DOMDataTreeShard> ListenerRegistration<T> registerDataTreeShard(final DOMDataTreeIdentifier prefix, final T shard) throws DOMDataTreeShardingConflictException {
        final ShardRegistration<T> reg;
        final ShardRegistration<?> parentReg;

        synchronized (this) {
            /*
             * Lookup the parent shard (e.g. the one which currently matches the prefix),
             * and if it exists, check if its registration prefix does not collide with
             * this registration.
             */
            final DOMDataTreePrefixTableEntry<ShardRegistration<?>> parent = shards.lookup(prefix);
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

            reg = new ShardRegistration<T>(this, prefix, shard);

            shards.store(prefix, reg);

            // FIXME: update any producers/registrations
        }

        // Notify the parent shard
        if (parentReg != null) {
            parentReg.getInstance().onChildAttached(prefix, shard);
        }

        return reg;
    }

    @GuardedBy("this")
    private DOMDataTreeProducer findProducer(final DOMDataTreeIdentifier subtree) {

        DOMDataTreePrefixTableEntry<DOMDataTreeProducer> producerEntry = producers.lookup(subtree);
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
    private DOMDataTreeProducer createProducer(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        // Record the producer's attachment points
        final DOMDataTreeProducer ret = ShardedDOMDataTreeProducer.create(this, shardMap);
        for (final DOMDataTreeIdentifier s : shardMap.keySet()) {
            producers.store(s, ret);
        }

        return ret;
    }

    @GuardedBy("this")
    public synchronized DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkArgument(!subtrees.isEmpty(), "Subtrees may not be empty");

        final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap = new HashMap<>();
        for (final DOMDataTreeIdentifier subtree : subtrees) {
            // Attempting to create a disconnected producer -- all subtrees have to be unclaimed
            final DOMDataTreeProducer producer = findProducer(subtree);
            Preconditions.checkArgument(producer == null, "Subtree %s is attached to producer %s", subtree, producer);

            shardMap.put(subtree, shards.lookup(subtree).getValue().getInstance());
        }

        return createProducer(shardMap);
    }

    synchronized DOMDataTreeProducer createProducer(final ShardedDOMDataTreeProducer parent, final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkNotNull(parent);

        final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap = new HashMap<>();
        for (final DOMDataTreeIdentifier s : subtrees) {
            shardMap.put(s, shards.lookup(s).getValue().getInstance());
        }

        return createProducer(shardMap);
    }

    @Override
    public synchronized <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges,
            final Collection<DOMDataTreeProducer> producers) throws DOMDataTreeLoopException {
        Preconditions.checkNotNull(listener, "listener");
        Preconditions.checkArgument(!subtrees.isEmpty(), "Subtrees must not be empty.");
        final ShardedDOMDataTreeListenerContext<T> listenerContext =
                ShardedDOMDataTreeListenerContext.create(listener, subtrees, allowRxMerges);
        try {
            // FIXME: Add attachment of producers
            for (DOMDataTreeProducer producer : producers) {
                Preconditions.checkArgument(producer instanceof ShardedDOMDataTreeProducer);
                ShardedDOMDataTreeProducer castedProducer = ((ShardedDOMDataTreeProducer) producer);
                simpleLoopCheck(subtrees, castedProducer.getSubtrees());
                // FIXME: We should also unbound listeners
                castedProducer.boundToListener(listenerContext);
            }

            for (DOMDataTreeIdentifier subtree : subtrees) {
                DOMDataTreeShard shard = shards.lookup(subtree).getValue().getInstance();
                // FIXME: What should we do if listener is wildcard? And shards are on per
                // node basis?
                Preconditions.checkArgument(shard instanceof DOMStoreTreeChangePublisher,
                        "Subtree %s does not point to listenable subtree.", subtree);

                listenerContext.register(subtree, (DOMStoreTreeChangePublisher) shard);
            }
        } catch (Exception e) {
            listenerContext.close();
            throw e;
        }
        return new AbstractListenerRegistration<T>(listener) {
            @Override
            protected void removeRegistration() {
                ShardedDOMDataTree.this.removeListener(listenerContext);
            }
        };
    }

    private static void simpleLoopCheck(Collection<DOMDataTreeIdentifier> listen, Set<DOMDataTreeIdentifier> writes)
            throws DOMDataTreeLoopException {
        for(DOMDataTreeIdentifier listenPath : listen) {
            for (DOMDataTreeIdentifier writePath : writes) {
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

    void removeListener(ShardedDOMDataTreeListenerContext<?> listener) {
        // FIXME: detach producers
        listener.close();
    }
}
