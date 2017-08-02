/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class implementing {@link DOMDataTreeListenerRegistry} and dealing with reconciling parent versus child
 * shard state.
 *
 * @author Robert Varga
 */
@Beta
public final class ShardedDOMDataTreeListenerRegistry implements DOMDataTreeListenerRegistry, SchemaContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeListenerRegistry.class);

    private final ShardedDOMDataTreeListenerRegistrationTree localListeners;

    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, ChildShardContext> childShards = ImmutableMap.of();

    public ShardedDOMDataTreeListenerRegistry(final DOMDataTreeIdentifier prefix,
            final DOMDataTreeListenerRegistry localRegistry) {
        this.localListeners = new ShardedDOMDataTreeListenerRegistrationTree(prefix, localRegistry);
    }

    @Override
    public synchronized <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges) {

        // Classify requested subtrees into two groups:
        // - those that involve local state
        // - those that involve only child shard state
        final Collection<DOMDataTreeIdentifier> localSubtrees;
        final Multimap<ChildShardContext, DOMDataTreeIdentifier> childSubtrees;
        if (!childShards.isEmpty()) {
            localSubtrees = new ArrayList<>(subtrees.size());
            childSubtrees = ArrayListMultimap.create();

            for (DOMDataTreeIdentifier subtree : subtrees) {
                final Optional<ChildShardContext> subShard = findChildShard(subtree);
                if (subShard.isPresent()) {
                    childSubtrees.put(subShard.get(), subtree);
                } else {
                    localSubtrees.add(subtree);
                }
            }
        } else {
            localSubtrees = subtrees;
            childSubtrees = ImmutableMultimap.of();
        }

        LOG.debug("Listener {} requested subtrees {} split into local {} and child {}", listener, subtrees,
            localSubtrees, childSubtrees);

        // Local state is not involved in this registration, all we need to do is to aggregate registrations coming
        // from child shards. If any of the children are detached it is expected that that shard will signal a failure,
        // cleaning up this listener.
        if (localSubtrees.isEmpty()) {
            LOG.debug("Listener {} is an aggregation of child shards {}", listener, childSubtrees);
            return DOMDataTreeListenerAggregator.aggregateIfNeeded(listener, childSubtrees.asMap(), allowRxMerges,
                ChildShardContext::getShard);
        }

        // Check if there is some overlap between localSubtrees and child shards and produce
        // a subtree -> subshard prefix map.
        final Multimap<DOMDataTreeIdentifier, DOMDataTreeIdentifier> mergeSubtrees;
        if (!childShards.isEmpty()) {
            mergeSubtrees = ArrayListMultimap.create();

            for (DOMDataTreeIdentifier subtree : localSubtrees) {
                for (DOMDataTreeIdentifier subShard : childShards.keySet()) {
                    if (subtree.contains(subShard)) {
                        mergeSubtrees.put(subtree, subShard);
                    }
                }
            }
        } else {
            mergeSubtrees = ImmutableMultimap.of();
        }

        // Foreign state is not involved, at least for now. We still need to track the listener to deal with the case
        // where a child shard is attached afterwards.
        if (childSubtrees.isEmpty()) {
            LOG.debug("Listener {} is an aggregation local subtrees {} merged with {}", listener, localSubtrees,
                mergeSubtrees);
            return registerLocalListener(listener, localSubtrees, mergeSubtrees, allowRxMerges);
        }

        // We have both local and foreign state involved, we need to instantiate an aggregator and feed it with both
        // local and remote state.
        LOG.debug("Listener {} is an aggregation of child subtrees {} and local subtrees {} merged with {}", listener,
            childSubtrees, localSubtrees, mergeSubtrees);
        final int sizeHint = childSubtrees.size() + 1;
        final Collection<ListenerRegistration<DOMDataTreeListener>> regs = new ArrayList<>(sizeHint);
        final DOMDataTreeListenerAggregator aggregator = new DOMDataTreeListenerAggregator(sizeHint, allowRxMerges);

        regs.add(registerLocalListener(aggregator.createListener(), localSubtrees, mergeSubtrees, allowRxMerges));
        childSubtrees.asMap().forEach((child, trees) -> {
            regs.add(CompatListenableDOMDataTreeShard.createIfNeeded(child.getShard())
                .registerListener(aggregator.createListener(), trees, allowRxMerges));
        });

        return aggregator.start(listener, regs);
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        localListeners.updateSchemaContext(context);
    }

    public synchronized void attachChild(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        checkArgument(child instanceof WriteableDOMDataTreeShard);
        final WriteableDOMDataTreeShard shard = (WriteableDOMDataTreeShard) child;

        final Map<DOMDataTreeIdentifier, ChildShardContext> newChildren = new HashMap<>(childShards);
        final ChildShardContext prev = newChildren.put(prefix, new ChildShardContext(prefix, shard));
        checkArgument(prev == null, "Attempted to attach child {} to prefix {}, which already has {}", child, prefix,
                prev);
        childShards = ImmutableMap.copyOf(newChildren);

        // Update listeners to starting merging, if needed
        localListeners.addChild(prefix, child);
    }

    public synchronized void detachChild(final DOMDataTreeIdentifier prefix) {
        final Map<DOMDataTreeIdentifier, ChildShardContext> newChildren = new HashMap<>(childShards);
        final ChildShardContext child = newChildren.remove(prefix);
        if (child == null) {
            LOG.warn("Attempted to detach child from {}, which is not present in {}", prefix, childShards);
            return;
        }

        // No further actions are necessary, as the listeners are expected to get cancelled by the detaching shard,
        // which will trigger us failing listeners and the app re-registering them.
        childShards = ImmutableMap.copyOf(newChildren);
    }

    @GuardedBy("this")
    private Optional<ChildShardContext> findChildShard(final DOMDataTreeIdentifier subtree) {
        return childShards.entrySet().stream().filter(entry -> entry.getKey().contains(subtree)).findAny()
                .map(Entry::getValue);
    }

    @GuardedBy("this")
    private <T extends DOMDataTreeListener> ListenerRegistration<T> registerLocalListener(final T userListener,
            final Collection<DOMDataTreeIdentifier> localSubtrees,
            final Multimap<DOMDataTreeIdentifier, DOMDataTreeIdentifier> mergeSubtrees, final boolean allowRxMerges) {
        if (mergeSubtrees.isEmpty()) {
            // Let's knock out the simple and most common case here. This listener is currently not affected by any
            // child shards, so all we have to do is to record it and attach it to local state. Future changes to
            // layout will be taken care of when they happen.
            return localListeners.createListener(userListener, localSubtrees, allowRxMerges);
        }

        // This is where the fun starts. We have a local listener on multiple trees, each of which is impacted by
        // a local change in state and potentially a set of downstream shards. The API contract requires us to do some
        // heavy lifting here, as we need to track an NxM matrix and perform state merges such that modifications to
        // subtrees look like they occurred in the local shard.

        // FIXME: implement this
        throw new UnsupportedOperationException("Listener merges are not implemented yet");
    }
}
