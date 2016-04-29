/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard.ChildShardContext;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.Invoker;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDOMDataTreeShardChangePublisher extends AbstractDOMShardTreeChangePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShardChangePublisher.class);

    private static final Invoker<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> MANAGER_INVOKER =
            new Invoker<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate>() {
                @Override
                public void invokeListener(final AbstractDOMDataTreeChangeListenerRegistration<?> listener, final DataTreeCandidate notification) {
                    // FIXME: this is inefficient, as we could grab the entire queue for the listener and post it
                    final DOMDataTreeChangeListener inst = listener.getInstance();
                    if (inst != null) {
                        inst.onDataTreeChanged(Collections.singletonList(notification));
                    }
                }
            };

    private final QueuedNotificationManager<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> notificationManager;

    private final ExecutorService executorService;
    private final Map<DOMDataTreeIdentifier, ChildShardContext> childShards;

    InMemoryDOMDataTreeShardChangePublisher(final ExecutorService executorService,
                                            final int maxQueueSize,
                                            final DataTree dataTree,
                                            final YangInstanceIdentifier rootPath,
                                            final Map<DOMDataTreeIdentifier, ChildShardContext> childShards) {
        super(dataTree, rootPath, childShards);
        this.executorService = executorService;
        this.childShards = childShards;
        notificationManager = new QueuedNotificationManager<>(executorService, MANAGER_INVOKER, maxQueueSize, "DataTreeChangeListenerQueueMgr");
    }

    @Override
    protected void notifyListeners(@Nonnull Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations, @Nonnull YangInstanceIdentifier path, @Nonnull DataTreeCandidateNode node) {
        final DataTreeCandidate candidate = DataTreeCandidates.newDataTreeCandidate(path, node);

        for (AbstractDOMDataTreeChangeListenerRegistration<?> reg : registrations) {
            LOG.debug("Enqueueing candidate {} to registration {}", candidate, registrations);
            notificationManager.submitNotification(reg, candidate);
        }
    }

    @Override
    protected void registrationRemoved(@Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        LOG.debug("Closing registration {}", registration);

    }

    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> registerTreeChangeListener(YangInstanceIdentifier path, L listener) {

        final AbstractDOMDataTreeChangeListenerRegistration<L> listenerRegistration = super.registerTreeChangeListener(path, listener);

        return listenerRegistration;
    }

    synchronized void publishChange(@Nonnull final DataTreeCandidate candidate) {
        processCandidateTree(candidate);
    }
}
