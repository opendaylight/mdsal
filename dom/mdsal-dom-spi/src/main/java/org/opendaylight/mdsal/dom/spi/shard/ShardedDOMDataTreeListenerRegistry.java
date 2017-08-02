/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeNode;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
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

    private final RegistrationTree localListeners;

    @GuardedBy("this")
    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards = ImmutableMap.of();

    public ShardedDOMDataTreeListenerRegistry(final DOMDataTreeIdentifier prefix,
            final DOMDataTreeListenerRegistry localRegistry) {
        this.localListeners = new RegistrationTree(prefix, localRegistry);
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
        // subtrees look like they occurred in the local shard. As a further headache that matrix can potentially change
        // in future and we also have to deal with atomic start of listeners with local state not being present.

        // FIXME: implement this
        throw new UnsupportedOperationException("Listener merges are not implemented yet");
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        localListeners.updateSchemaContext(context);
    }

    @GuardedBy("this")
    private Optional<ChildShardContext> findChildShard(final DOMDataTreeIdentifier subtree) {
        return childShards.entrySet().stream().filter(entry -> entry.getKey().contains(subtree)).findAny()
                .map(Entry::getValue);
    }

    private static final class ShardedListenerRegistration<T extends DOMDataTreeListener>
            extends AbstractListenerRegistration<T> implements DOMDataTreeListener {
        @GuardedBy("this")
        private final ListenerRegistration<?> localReg;
        @GuardedBy("this")
        private SchemaContext schemaContext;
        @GuardedBy("this")
        private Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> localSubtrees;

        ShardedListenerRegistration(final DOMDataTreeListenerRegistry localRegistry, final SchemaContext schemaContext,
                final T userListener, final Collection<DOMDataTreeIdentifier> localSubtrees,
                final boolean allowRxMerges) {
            super(userListener);
            this.schemaContext = requireNonNull(schemaContext);
            this.localReg = localRegistry.registerListener(this, localSubtrees, allowRxMerges);
        }

        synchronized void updateSchemaContext(final SchemaContext schemaContext) {
            this.schemaContext = requireNonNull(schemaContext);
        }

        @Override
        public synchronized void onDataTreeChanged(final Collection<DataTreeCandidate> changes,
                final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
            getInstance().onDataTreeChanged(changes, subtrees);
            this.localSubtrees = ImmutableMap.copyOf(subtrees);
        }

        @Override
        public synchronized void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
            localReg.close();
            getInstance().onDataTreeFailed(causes);
        }

        @Override
        protected synchronized void removeRegistration() {
            localReg.close();
            // FIXME: upcall to RegistrationTree to remove the registration
        }
    }

    private static final class RegistrationTree extends AbstractRegistrationTree<ShardedListenerRegistration<?>> {
        private final DOMDataTreeListenerRegistry localRegistry;
        private final DOMDataTreeIdentifier prefix;
        private SchemaContext schemaContext;

        RegistrationTree(final DOMDataTreeIdentifier prefix, final DOMDataTreeListenerRegistry localRegistry) {
            this.prefix = requireNonNull(prefix);
            this.localRegistry = requireNonNull(localRegistry);
        }

        void updateSchemaContext(final SchemaContext schemaContext) {
            takeLock();
            try {
                this.schemaContext = requireNonNull(schemaContext);
                updateSchemaContext(findNodeFor(ImmutableList.of()), schemaContext);
            } finally {
                releaseLock();
            }
        }

        <T extends DOMDataTreeListener> ShardedListenerRegistration<T> createListener(final T userListener,
                final Collection<DOMDataTreeIdentifier> localSubtrees, final boolean allowRxMerges) {
            final Collection<Collection<PathArgument>> paths = subtreesToPaths(localSubtrees);

            takeLock();
            try {
                // Instantiate and start the listener
                final ShardedListenerRegistration<T> ret = new ShardedListenerRegistration<>(localRegistry,
                        schemaContext, userListener, localSubtrees, allowRxMerges);

                // Register it with all the nodes it belongs to
                for (Collection<PathArgument> path : paths) {
                    addRegistration(findNodeFor(path), ret);
                }

                return ret;
            } finally {
                releaseLock();
            }
        }

        private static void updateSchemaContext(final RegistrationTreeNode<ShardedListenerRegistration<?>> node,
                final SchemaContext schemaContext) {
            node.getRegistrations().forEach(reg -> reg.updateSchemaContext(schemaContext));
            node.forEachChild(child -> updateSchemaContext(child, schemaContext));
        }

        private Collection<Collection<PathArgument>> subtreesToPaths(final Collection<DOMDataTreeIdentifier> subtrees) {
            return subtrees.stream().map(treeId -> {
                final com.google.common.base.Optional<YangInstanceIdentifier> optPath = treeId.getRootIdentifier()
                        .relativeTo(prefix.getRootIdentifier());
                checkArgument(optPath.isPresent(), "Requested subtree %s is not a subtree of %s", treeId, prefix);
                return optPath.get().getPathArguments();
            }).collect(Collectors.toList());
        }
    }
}
