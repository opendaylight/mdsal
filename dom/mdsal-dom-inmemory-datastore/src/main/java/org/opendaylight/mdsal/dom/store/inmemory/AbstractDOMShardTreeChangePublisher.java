/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeNode;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard.ChildShardContext;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

abstract class AbstractDOMShardTreeChangePublisher extends AbstractDOMStoreTreeChangePublisher implements DOMStoreTreeChangePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMShardTreeChangePublisher.class);

    private YangInstanceIdentifier shardPath;
    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards;
    private final DataTree dataTree;

    protected AbstractDOMShardTreeChangePublisher(final DataTree dataTree,
                                                  final YangInstanceIdentifier shardPath,
                                                  final Map<DOMDataTreeIdentifier, ChildShardContext> childShards) {
        this.dataTree = dataTree;
        this.shardPath = shardPath;
        this.childShards = childShards;
    }

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> registerTreeChangeListener(final YangInstanceIdentifier path, final L listener) {
        takeLock();
        try {
            return setupListenerContext(path, listener);
        } finally {
            releaseLock();
        }
    }

    private <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> setupListenerContext(final YangInstanceIdentifier listenerPath, final L listener) {

        final DOMDataTreeListenerWithSubshards subshardListener = new DOMDataTreeListenerWithSubshards(dataTree, listener);
        final AbstractDOMDataTreeChangeListenerRegistration<L> reg = setupContextWithoutSubshards(listenerPath, subshardListener);

        for (final ChildShardContext maybeAffected : childShards.values()) {
            if (listenerPath.contains(maybeAffected.getPrefix().getRootIdentifier())) {
                // consumer has a subshard somewhere on lower level
                // register to the notification manager with snapshot and forward child notifications to parent
                LOG.debug("Adding new subshard{{}} to listener at {}", maybeAffected.getPrefix(), listenerPath);
                subshardListener.addSubshard(maybeAffected);
            } else if (maybeAffected.getPrefix().getRootIdentifier().contains(listenerPath)) {
                // bind path is inside subshard
                // TODO can this happen? seems like in ShardedDOMDataTree we are already registering to the lowest shard possible
                throw new NotImplementedException();
            }
        }
        return reg;
    }

    private <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> setupContextWithoutSubshards(final YangInstanceIdentifier listenerPath, final DOMDataTreeListenerWithSubshards listener) {
        LOG.debug("Registering root listener");

        // we need to register the listener registration path based on the shards root,
        // we have to strip the shard path from the listener path and then register
        final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node = findNodeFor(stripShardPath(listenerPath));
        final AbstractDOMDataTreeChangeListenerRegistration<L> registration = new AbstractDOMDataTreeChangeListenerRegistration<L>((L) listener) {
            @Override
            protected void removeRegistration() {
                listener.close();
                AbstractDOMShardTreeChangePublisher.this.removeRegistration(node, this);
                registrationRemoved(this);
            }
        };
        addRegistration(node, registration);
        return registration;
    }

    private Iterable<PathArgument> stripShardPath(final YangInstanceIdentifier listenerPath) {
        if (shardPath.getPathArguments().isEmpty()) {
            return listenerPath.getPathArguments();
        }

        final ArrayList<PathArgument> shardPathArgs = new ArrayList<>(shardPath.getPathArguments());
        final ArrayList<PathArgument> listenerPathArgs = new ArrayList<>(listenerPath.getPathArguments());
        final Iterator<PathArgument> shardIter = shardPathArgs.iterator();
        final Iterator<PathArgument> listenerIter = listenerPathArgs.iterator();

        while (shardIter.hasNext()) {
            if (shardIter.next().equals(listenerIter.next())) {
                shardIter.remove();
                listenerIter.remove();
            } else {
                break;
            }
        }

        return listenerPathArgs;

    }

    private static final class DOMDataTreeListenerWithSubshards implements DOMDataTreeChangeListener {

        // TODO should we synchronize the access to the dataTree snapshots?
        private final DataTree dataTree;
        private final DOMDataTreeChangeListener delegate;

        private final Map<YangInstanceIdentifier, ListenerRegistration<SubshardDOMListener>> registrations =
                new HashMap<>();

        DOMDataTreeListenerWithSubshards(final DataTree dataTree,
                                         final DOMDataTreeChangeListener delegate) {
            this.dataTree = dataTree;
            this.delegate = delegate;
        }

        @Override
        public void onDataTreeChanged(@Nonnull final Collection<DataTreeCandidate> changes) {
            // TODO figure out how to merge a single change that spans into a subshard
            LOG.warn("Received data changed {}", changes.iterator().next());
            final DataTreeCandidate newCandidate = applyChanges(changes);
            delegate.onDataTreeChanged(Collections.singleton(newCandidate));
        }

        void onDataTreeChanged(final YangInstanceIdentifier rootPath, final Collection<DataTreeCandidate> changes) {
            onDataTreeChanged(Collections2.transform(changes, new Function<DataTreeCandidate, DataTreeCandidate>() {
                @Nullable
                @Override
                public DataTreeCandidate apply(final DataTreeCandidate input) {
                    return DataTreeCandidates.newDataTreeCandidate(rootPath, input.getRootNode());
                }
            }));
        }

        void addSubshard(final ChildShardContext context) {
            Preconditions.checkState(context.getShard() instanceof DOMStoreTreeChangePublisher,
                    "All subshards that are a part of ListenerContext need to be listenable");

            final DOMStoreTreeChangePublisher listenableShard = (DOMStoreTreeChangePublisher) context.getShard();
            // since this is going into subshard we want to listen for ALL changes in the subshard
            registrations.put(context.getPrefix().getRootIdentifier(),
                    listenableShard.registerTreeChangeListener(context.getPrefix().getRootIdentifier(),
                            new SubshardDOMListener(context.getPrefix().getRootIdentifier(), this)));
        }

        void close() {
            for (final Entry<YangInstanceIdentifier, ListenerRegistration<SubshardDOMListener>> registration : registrations.entrySet()) {
                registration.getValue().close();
            }
            registrations.clear();
        }

        private DataTreeCandidate applyChanges(final Collection<DataTreeCandidate> changes) {
            final DataTreeModification modification = dataTree.takeSnapshot().newModification();
            for (final DataTreeCandidate change : changes) {
                DataTreeCandidates.applyToModification(modification, change);
            }
            modification.ready();
            try {
                dataTree.validate(modification);
            } catch (DataValidationFailedException e) {
                LOG.error("Validation failed for built modification", e);
                throw new RuntimeException("Validation failed", e);
            }
            return dataTree.prepare(modification);
        }
    }

    private static final class SubshardDOMListener implements DOMDataTreeChangeListener {

        private YangInstanceIdentifier path;
        private DOMDataTreeListenerWithSubshards delegate;

        SubshardDOMListener(final YangInstanceIdentifier path, final DOMDataTreeListenerWithSubshards delegate) {

            this.path = path;
            this.delegate = delegate;
        }

        @Override
        public void onDataTreeChanged(@Nonnull final Collection<DataTreeCandidate> changes) {
            LOG.warn("Subshard data changed {}", changes.toString());
            delegate.onDataTreeChanged(path, changes);
        }
    }

}
