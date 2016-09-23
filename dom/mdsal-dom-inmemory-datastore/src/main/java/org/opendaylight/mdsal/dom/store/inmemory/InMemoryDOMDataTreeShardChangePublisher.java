/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
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

    private final QueuedNotificationManager<AbstractDOMDataTreeChangeListenerRegistration<?>,
        DataTreeCandidate> notificationManager;

    @GuardedBy("this")
    private Multimap< AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> notifications;

    InMemoryDOMDataTreeShardChangePublisher(final Executor executor,
                                            final int maxQueueSize,
                                            final DataTree dataTree,
                                            final YangInstanceIdentifier rootPath,
                                            final Map<DOMDataTreeIdentifier, ChildShardContext> childShards) {
        super(dataTree, rootPath, childShards);
        notificationManager = QueuedNotificationManager.create(
                executor, MANAGER_INVOKER, maxQueueSize, "DataTreeChangeListenerQueueMgr");
    }

    // Guard guaranteed by superclass contract and publishChange() being synchronized
    @GuardedBy("this")
    @Override
    protected void notifyListeners(
            @Nonnull final Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations,
                                   @Nonnull final YangInstanceIdentifier path,
                                   @Nonnull final DataTreeCandidateNode node) {
        Verify.verifyNotNull(notifications);

        final DataTreeCandidate candidate = DataTreeCandidates.newDataTreeCandidate(path, node);

        for (final AbstractDOMDataTreeChangeListenerRegistration<?> reg : registrations) {
            final DOMDataTreeChangeListener listener = reg.getInstance();
            if (listener != null) {
                notifications.put(reg, candidate);
            } else {
                LOG.debug("Skipped candidate for closed registration {}", candidate, reg);
            }
        }
    }

    @Override
    protected void registrationRemoved(@Nonnull final AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        LOG.debug("Closing registration {}", registration);

    }

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L>
            registerTreeChangeListener(final YangInstanceIdentifier path, final L listener) {
        return super.registerTreeChangeListener(path, listener);
    }

    synchronized void publishChange(@Nonnull final DataTreeCandidate candidate) {
        Preconditions.checkState(notifications == null);
        notifications = MultimapBuilder.hashKeys().arrayListValues().build();
        try {
            processCandidateTree(candidate);

            for (Entry< AbstractDOMDataTreeChangeListenerRegistration<?>, Collection<DataTreeCandidate>> e :
                notifications.asMap().entrySet()) {
                LOG.debug("Enqueueing candidates {} to listener {}", e.getValue(), e.getKey());
                notificationManager.submitNotifications(e.getKey(), e.getValue());
            }
        } finally {
            notifications = null;
        }
    }
}
