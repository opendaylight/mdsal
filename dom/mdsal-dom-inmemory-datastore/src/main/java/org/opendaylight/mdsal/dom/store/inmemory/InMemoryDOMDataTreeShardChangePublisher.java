/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.shard.ChildShardContext;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDOMDataTreeShardChangePublisher extends AbstractDOMShardTreeChangePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShardChangePublisher.class);

    private static final BatchedInvoker<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate>
        MANAGER_INVOKER = (listener, notifications) -> {
            final DOMDataTreeChangeListener inst = listener.getInstance();
            if (inst != null) {
                inst.onDataTreeChanged(ImmutableList.copyOf(notifications));
            }
        };

    private final QueuedNotificationManager<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate>
        notificationManager;

    InMemoryDOMDataTreeShardChangePublisher(final Executor executor,
                                            final int maxQueueSize,
                                            final DataTree dataTree,
                                            final YangInstanceIdentifier rootPath,
                                            final Map<DOMDataTreeIdentifier, ChildShardContext> childShards) {
        super(dataTree, rootPath, childShards);
        notificationManager = QueuedNotificationManager.create(executor, MANAGER_INVOKER, maxQueueSize,
            "DataTreeChangeListenerQueueMgr");
    }

    @Override
    protected void notifyListener(final AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            final Collection<DataTreeCandidate> changes) {
        LOG.debug("Enqueueing candidates {} for registration {}", changes, registration);
        notificationManager.submitNotifications(registration, changes);
    }

    @Override
    protected void registrationRemoved(final AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        LOG.debug("Closing registration {}", registration);

    }

    synchronized void publishChange(final DataTreeCandidate candidate) {
        processCandidateTree(candidate);
    }
}
