/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.WriteableDOMDataTreeShard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ShardedDOMDataTreeProducer implements DOMDataTreeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducer.class);
    private final Map<DOMDataTreeIdentifier, DOMDataTreeShard> idToShard;
    private final ShardedDOMDataTree dataTree;

    private final BiMap<DOMDataTreeShard, DOMDataTreeShardProducer> shardToProducer;
    private final BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer;

    @GuardedBy("this")
    private DOMDataTreeCursorAwareTransaction openTx;
    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children = Collections.emptyMap();
    @GuardedBy("this")
    private boolean closed;

    @GuardedBy("this")
    private ShardedDOMDataTreeListenerContext<?> attachedListener;

    ShardedDOMDataTreeProducer(final ShardedDOMDataTree dataTree,
                               final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap,
                               final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToId) {
        this.dataTree = Preconditions.checkNotNull(dataTree);

        final Builder<DOMDataTreeShard, DOMDataTreeShardProducer> builder = ImmutableBiMap.builder();
        final Builder<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducerBuilder = ImmutableBiMap.builder();
        for (final Entry<DOMDataTreeShard, Collection<DOMDataTreeIdentifier>> entry : shardToId.asMap().entrySet()) {
            if (entry.getKey() instanceof WriteableDOMDataTreeShard) {
                //create a single producer for all prefixes in a single shard
                final DOMDataTreeShardProducer producer = ((WriteableDOMDataTreeShard) entry.getKey()).createProducer(entry.getValue());
                builder.put(entry.getKey(), producer);

                // id mapped to producers
                for (final DOMDataTreeIdentifier id : entry.getValue()) {
                    idToProducerBuilder.put(id, producer);
                }
            } else {
                LOG.error("Unable to create a producer for shard that's not a WriteableDOMDataTreeShard");
            }
        }
        this.shardToProducer = builder.build();
        this.idToProducer = idToProducerBuilder.build();
        idToShard = ImmutableMap.copyOf(shardMap);
    }

    static DOMDataTreeProducer create(final ShardedDOMDataTree dataTree, final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToIdetifiers = ArrayListMultimap.create();
        // map which identifier belongs to which shard
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShard> entry : shardMap.entrySet()) {
            shardToIdetifiers.put(entry.getValue(), entry.getKey());
        }

        return new ShardedDOMDataTreeProducer(dataTree, shardMap, shardToIdetifiers);
    }

    @Override
    public synchronized DOMDataTreeCursorAwareTransaction createTransaction(final boolean isolated) {
        Preconditions.checkState(!closed, "Producer is already closed");
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);

        this.openTx = new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children);

        return openTx;
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

    boolean isDelegatedToChild(final DOMDataTreeIdentifier path) {
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

    Set<DOMDataTreeIdentifier> getSubtrees() {
        return idToShard.keySet();
    }

    synchronized void cancelTransaction(final ShardedDOMDataTreeWriteTransaction transaction) {
        if (!openTx.equals(transaction)) {
            LOG.warn("Transaction {} is not open in producer {}", transaction, this);
            return;
        }

        LOG.debug("Transaction {} cancelled", transaction);
        openTx = null;
    }

    synchronized void transactionSubmitted(final ShardedDOMDataTreeWriteTransaction transaction) {
        Preconditions.checkState(openTx.equals(transaction));
        openTx = null;
    }

    synchronized void boundToListener(final ShardedDOMDataTreeListenerContext<?> listener) {
        // FIXME: Add option to dettach
        Preconditions.checkState(this.attachedListener == null,
                "Producer %s is already attached to other listener.",
                listener.getListener());
        this.attachedListener = listener;
    }
}
