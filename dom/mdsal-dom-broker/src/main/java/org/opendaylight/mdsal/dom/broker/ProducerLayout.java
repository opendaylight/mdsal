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
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.store.inmemory.WriteableDOMDataTreeShard;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

final class ProducerLayout {
    private final BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer;
    private final Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children;
    private final Map<DOMDataTreeIdentifier, DOMDataTreeShard> idToShard;

    private ProducerLayout(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap,
            final BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer,
            final Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children) {
        this.idToShard = ImmutableMap.copyOf(shardMap);
        this.idToProducer = Preconditions.checkNotNull(idToProducer);
        this.children = Preconditions.checkNotNull(children);
    }

    static ProducerLayout create(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        return new ProducerLayout(shardMap, mapIdsToProducer(shardMap), ImmutableMap.of());
    }

    private static BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> mapIdsToProducer(
            final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToId = ArrayListMultimap.create();
        // map which identifier belongs to which shard
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShard> entry : shardMap.entrySet()) {
            shardToId.put(entry.getValue(), entry.getKey());
        }

        final Builder<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducerBuilder = ImmutableBiMap.builder();
        for (final Entry<DOMDataTreeShard, Collection<DOMDataTreeIdentifier>> entry : shardToId.asMap().entrySet()) {
            if (entry.getKey() instanceof WriteableDOMDataTreeShard) {
                //create a single producer for all prefixes in a single shard
                final DOMDataTreeShardProducer producer = ((WriteableDOMDataTreeShard) entry.getKey())
                        .createProducer(entry.getValue());
                // id mapped to producers
                for (final DOMDataTreeIdentifier id : entry.getValue()) {
                    idToProducerBuilder.put(id, producer);
                }
            } else {
                ShardedDOMDataTreeProducer.LOG.error("Unable to create a producer for shard that's not a WriteableDOMDataTreeShard");
            }
        }

        return idToProducerBuilder.build();
    }

    ProducerLayout addChild(final DOMDataTreeProducer producer, final Collection<DOMDataTreeIdentifier> subtrees) {
        final ImmutableMap.Builder<DOMDataTreeIdentifier, DOMDataTreeProducer> cb = ImmutableMap.builder();
        cb.putAll(children);
        for (final DOMDataTreeIdentifier s : subtrees) {
            cb.put(s, producer);
        }

        final ImmutableMap<DOMDataTreeIdentifier, DOMDataTreeProducer> newChildren = cb.build();
        return new ProducerLayout(idToShard, idToProducer, newChildren);
    }

    ProducerLayout reshard(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        return new ProducerLayout(shardMap, mapIdsToProducer(shardMap), children);
    }

    boolean haveSubtree(final DOMDataTreeIdentifier subtree) {
        for (final DOMDataTreeIdentifier i : idToShard.keySet()) {
            if (i.contains(subtree)) {
                return true;
            }
        }

        return false;
    }

    DOMDataTreeProducer lookupChild(final DOMDataTreeIdentifier path) {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeProducer> e : children.entrySet()) {
            if (e.getKey().contains(path)) {
                // FIXME: does this match wildcards?
                return e.getValue();
            }
        }

        return null;
    }

    Set<DOMDataTreeIdentifier> getChildTrees() {
        return children.keySet();
    }

    void checkAvailable(final YangInstanceIdentifier path) {
        for (final DOMDataTreeIdentifier c : children.keySet()) {
            Preconditions.checkArgument(!c.getRootIdentifier().contains(path),
                "Path {%s} is not available to this cursor since it's already claimed by a child producer", path);
        }
    }

    Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> createTransactions() {
        return Maps.transformValues(idToProducer, DOMDataTreeShardProducer::createTransaction);
    }
}
