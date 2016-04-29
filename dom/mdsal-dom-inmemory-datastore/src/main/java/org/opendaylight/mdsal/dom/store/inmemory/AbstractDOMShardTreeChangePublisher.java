/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.RegistrationTreeNode;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard.ChildShardContext;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

abstract class AbstractDOMShardTreeChangePublisher extends AbstractDOMStoreTreeChangePublisher implements DOMStoreTreeChangePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMShardTreeChangePublisher.class);

    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards;

    protected AbstractDOMShardTreeChangePublisher(final Map<DOMDataTreeIdentifier, ChildShardContext> childShards) {
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

        final DOMDataTreeListenerWithSubshards subshardListener = new DOMDataTreeListenerWithSubshards(listenerPath, listener);
        final AbstractDOMDataTreeChangeListenerRegistration<L> reg = setupContextWithoutSubshards(listenerPath, subshardListener);

        for (final ChildShardContext maybeAffected : childShards.values()) {
            final YangInstanceIdentifier bindPath;
            if (listenerPath.contains(maybeAffected.getPrefix().getRootIdentifier())) {
                // consumer has a subshard somewhere on lower level
                // register to the notification manager with snapshot and forward child notifications to parent
                subshardListener.addSubshard(maybeAffected);
            } else if (maybeAffected.getPrefix().getRootIdentifier().contains(listenerPath)) {
                // bind path is inside subshard
                // only forward notifications from child to parent
                // TODO can this happen? seems like in ShardedDOMDataTree we are already registering to the lowest shard possible

                throw new NotImplementedException();
            }
        }
        return reg;
    }

    private <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> setupContextWithoutSubshards(final YangInstanceIdentifier listenerPath, final DOMDataTreeListenerWithSubshards listener) {
        LOG.debug("Registering listener with no subshards");

        final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node = findNodeFor(listenerPath.getPathArguments());
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

    private static final class DOMDataTreeListenerWithSubshards implements DOMDataTreeChangeListener {

        private final DOMDataTreeChangeListener delegate;
        private final YangInstanceIdentifier root;
        private final Map<YangInstanceIdentifier, NormalizedNode<?, ?>> currentSubshardData = new HashMap<>();

        private final Map<YangInstanceIdentifier, ListenerRegistration<DOMDataTreeListenerWithSubshards>> registrations =
                new HashMap<>();

        DOMDataTreeListenerWithSubshards(final YangInstanceIdentifier root, final DOMDataTreeChangeListener delegate) {

            this.delegate = delegate;
            this.root = root;
        }

        @Override
        public void onDataTreeChanged(@Nonnull final Collection<DataTreeCandidate> changes) {
            delegate.onDataTreeChanged(changes);
        }

        void addSubshard(final ChildShardContext context) {
            Preconditions.checkState(context.getShard() instanceof DOMStoreTreeChangePublisher,
                    "All subshards that are a part of ListenerContext need to be listenable");

            final DOMStoreTreeChangePublisher listenableShard = (DOMStoreTreeChangePublisher) context.getShard();
            // since this is going into subshard we want to listen for ALL changes in the subshard
            registrations.put(context.getPrefix().getRootIdentifier(),
                    listenableShard.registerTreeChangeListener(context.getPrefix().getRootIdentifier(), this));
        }

        void close() {
            for (final Entry<YangInstanceIdentifier, ListenerRegistration<DOMDataTreeListenerWithSubshards>> registration : registrations.entrySet()) {
                registration.getValue().close();
            }
            registrations.clear();
        }
    }

}
