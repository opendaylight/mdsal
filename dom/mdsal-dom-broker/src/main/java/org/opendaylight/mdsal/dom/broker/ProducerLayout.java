/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.spi.shard.WriteableDOMDataTreeShard;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProducerLayout {
    private static final Logger LOG = LoggerFactory.getLogger(ProducerLayout.class);

    private final BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer;
    private final Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children;
    private final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap;

    private ProducerLayout(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap,
            final BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer,
            final Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children) {
        this.shardMap = ImmutableMap.copyOf(shardMap);
        this.idToProducer = requireNonNull(idToProducer);
        this.children = requireNonNull(children);
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
                LOG.error("Unable to create a producer for shard that's not a WriteableDOMDataTreeShard");
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

        return new ProducerLayout(shardMap, idToProducer, cb.build());
    }

    ProducerLayout reshard(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> newShardMap) {
        close();
        return new ProducerLayout(newShardMap, mapIdsToProducer(newShardMap), children);
    }

    boolean haveSubtree(final DOMDataTreeIdentifier subtree) {
        for (final DOMDataTreeIdentifier i : shardMap.keySet()) {
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

    void checkAvailable(final Collection<PathArgument> base, final PathArgument child) {
        if (!children.isEmpty()) {
            final Collection<PathArgument> args = new ArrayList<>(base.size() + 1);
            args.addAll(base);
            args.add(child);

            final YangInstanceIdentifier path = YangInstanceIdentifier.create(args);
            for (final DOMDataTreeIdentifier c : children.keySet()) {
                Preconditions.checkArgument(!c.getRootIdentifier().contains(path),
                    "Path {%s} is not available to this cursor since it's already claimed by a child producer", path);
            }
        }
    }

    Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> createTransactions() {
        Preconditions.checkState(!idToProducer.isEmpty(),
                "Cannot create transaction since the producer is not mapped to any shard");
        return Maps.transformValues(idToProducer, DOMDataTreeShardProducer::createTransaction);
    }

    void close() {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShardProducer> entry : idToProducer.entrySet()) {
            ((WriteableDOMDataTreeShard) requireNonNull(shardMap.get(entry.getKey()))).closeProducer(entry.getValue());
        }
    }

}
