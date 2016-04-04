/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ShardedDOMDataTreeProducer implements DOMDataTreeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducer.class);
    private final BiMap<DOMDataTreeShard, DOMStoreTransactionChain> shardToChain;
    private final Map<DOMDataTreeIdentifier, DOMDataTreeShard> idToShard;
    private final ShardedDOMDataTree dataTree;

    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children = Collections.emptyMap();
    @GuardedBy("this")
    private DOMDataTreeWriteTransaction openTx;
    @GuardedBy("this")
    private boolean closed;

    @GuardedBy("this")
    private ShardedDOMDataTreeListenerContext<?> attachedListener;

    ShardedDOMDataTreeProducer(final ShardedDOMDataTree dataTree, final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap, final Set<DOMDataTreeShard> shards) {
        this.dataTree = Preconditions.checkNotNull(dataTree);

        // Create shard -> chain map
        final Builder<DOMDataTreeShard, DOMStoreTransactionChain> cb = ImmutableBiMap.builder();
        final Queue<Exception> es = new LinkedList<>();

        for (final DOMDataTreeShard s : shards) {
            if (s instanceof DOMStore) {
                try {
                    final DOMStoreTransactionChain c = ((DOMStore)s).createTransactionChain();
                    LOG.trace("Using DOMStore chain {} to access shard {}", c, s);
                    cb.put(s, c);
                } catch (final Exception e) {
                    LOG.error("Failed to instantiate chain for shard {}", s, e);
                    es.add(e);
                }
            } else {
                LOG.error("Unhandled shard instance type {}", s.getClass());
            }
        }
        this.shardToChain = cb.build();

        // An error was encountered, close chains and report the error
        if (shardToChain.size() != shards.size()) {
            for (final DOMStoreTransactionChain c : shardToChain.values()) {
                try {
                    c.close();
                } catch (final Exception e) {
                    LOG.warn("Exception raised while closing chain {}", c, e);
                }
            }

            final IllegalStateException e = new IllegalStateException("Failed to completely allocate contexts", es.poll());
            while (!es.isEmpty()) {
                e.addSuppressed(es.poll());
            }

            throw e;
        }

        idToShard = ImmutableMap.copyOf(shardMap);
    }

    @Override
    public synchronized DOMDataTreeWriteTransaction createTransaction(final boolean isolated) {
        Preconditions.checkState(!closed, "Producer is already closed");
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);

        // Allocate backing transactions
        final Map<DOMDataTreeShard, DOMStoreWriteTransaction> shardToTx = new HashMap<>();
        for (final Entry<DOMDataTreeShard, DOMStoreTransactionChain> e : shardToChain.entrySet()) {
            shardToTx.put(e.getKey(), e.getValue().newWriteOnlyTransaction());
        }

        // Create the ID->transaction map
        final ImmutableMap.Builder<DOMDataTreeIdentifier, DOMStoreWriteTransaction> b = ImmutableMap.builder();
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShard> e : idToShard.entrySet()) {
            b.put(e.getKey(), shardToTx.get(e.getValue()));
        }

        final ShardedDOMDataWriteTransaction ret = new ShardedDOMDataWriteTransaction(this, b.build());
        openTx = ret;
        return ret;
    }

    @GuardedBy("this")
    private boolean haveSubtree(final DOMDataTreeIdentifier subtree) {
        for (final DOMDataTreeIdentifier i : idToShard.keySet()) {
            if (i.contains(subtree)) {
                return true;
            }
        }

        return false;
    }

    @GuardedBy("this")
    private DOMDataTreeProducer lookupChild(final DOMDataTreeIdentifier s) {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeProducer> e : children.entrySet()) {
            if (e.getKey().contains(s)) {
                return e.getValue();
            }
        }

        return null;
    }

    @Override
    public synchronized DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkState(!closed, "Producer is already closed");
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);

        for (final DOMDataTreeIdentifier s : subtrees) {
            // Check if the subtree was visible at any time
            Preconditions.checkArgument(haveSubtree(s), "Subtree %s was never available in producer %s", s, this);
            // Check if the subtree has not been delegated to a child
            final DOMDataTreeProducer child = lookupChild(s);
            Preconditions.checkArgument(child == null, "Subtree %s is delegated to child producer %s", s, child);

            // Check if part of the requested subtree is not delegated to a child.
            for (final DOMDataTreeIdentifier c : children.keySet()) {
                if (s.contains(c)) {
                    throw new IllegalArgumentException(String.format("Subtree %s cannot be delegated as it is superset of already-delegated %s", s, c));
                }
            }
        }

        final DOMDataTreeProducer ret = dataTree.createProducer(this, subtrees);
        final ImmutableMap.Builder<DOMDataTreeIdentifier, DOMDataTreeProducer> cb = ImmutableMap.builder();
        cb.putAll(children);
        for (final DOMDataTreeIdentifier s : subtrees) {
            cb.put(s, ret);
        }

        children = cb.build();
        return ret;
    }

    boolean isDelegatedToChild(DOMDataTreeIdentifier path) {
        for (final DOMDataTreeIdentifier c : children.keySet()) {
            if (c.contains(path)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public synchronized void close() throws DOMDataTreeProducerException {
        if (!closed) {
            if (openTx != null) {
                throw new DOMDataTreeProducerBusyException(String.format("Transaction %s is still open", openTx));
            }

            closed = true;
            dataTree.destroyProducer(this);
        }
    }

    static DOMDataTreeProducer create(final ShardedDOMDataTree dataTree, final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        /*
         * FIXME: we do not allow multiple multiple shards in a producer because we do not implement the
         *        synchronization primitives yet
         */
        final Set<DOMDataTreeShard> shards = ImmutableSet.copyOf(shardMap.values());
        if (shards.size() > 1) {
            throw new UnsupportedOperationException("Cross-shard producers are not supported yet");
        }

        return new ShardedDOMDataTreeProducer(dataTree, shardMap, shards);
    }

    public Set<DOMDataTreeIdentifier> getSubtrees() {
        return idToShard.keySet();
    }

    synchronized void cancelTransaction(final ShardedDOMDataWriteTransaction transaction) {
        if (!openTx.equals(transaction)) {
            LOG.warn("Transaction {} is not open in producer {}", transaction, this);
            return;
        }

        LOG.debug("Transaction {} cancelled", transaction);
        openTx = null;
    }

    synchronized void transactionSubmitted(ShardedDOMDataWriteTransaction transaction) {
        Preconditions.checkState(openTx.equals(transaction));
        openTx = null;
    }

    synchronized void boundToListener(ShardedDOMDataTreeListenerContext<?> listener) {
        // FIXME: Add option to dettach
        Preconditions.checkState(this.attachedListener == null,
                "Producer %s is already attached to other listener.",
                listener.getListener());
        this.attachedListener = listener;
    }
}
