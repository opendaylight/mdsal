/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.concurrent.EqualityQueuedNotificationManager;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDOMStoreTreeChangePublisher extends AbstractDOMStoreTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMStoreTreeChangePublisher.class);

    // Registrations use identity for equality, hence we can skip wrapping them
    private final EqualityQueuedNotificationManager<Reg, DataTreeCandidate> notificationManager;

    InMemoryDOMStoreTreeChangePublisher(final String dsName, final ExecutorService listenerExecutor,
            final int maxQueueSize) {
        notificationManager = new EqualityQueuedNotificationManager<>("DataTreeChangeListenerQueueMgr + dsName",
            listenerExecutor, maxQueueSize,
            (listener, notifications) -> {
                if (listener.notClosed()) {
                    listener.listener().onDataTreeChanged(notifications);
                }
            });
    }

    private InMemoryDOMStoreTreeChangePublisher(
            final EqualityQueuedNotificationManager<Reg, DataTreeCandidate> notificationManager) {
        this.notificationManager = notificationManager;
    }

    EqualityQueuedNotificationManager<?, ?> getNotificationManager() {
        return notificationManager;
    }

    @Override
    protected void notifyListener(final Reg registration, final List<DataTreeCandidate> changes) {
        LOG.debug("Enqueueing candidates {} for registration {}", changes, registration);
        notificationManager.submitNotifications(registration, changes);
    }

    @Override
    protected synchronized void registrationRemoved(final Reg registration) {
        LOG.debug("Closing registration {}", registration);

        // FIXME: remove the queue for this registration and make sure we clear it
    }

    Registration registerTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener, final DataTreeSnapshot snapshot) {
        final var reg = registerTreeChangeListener(treeId, listener);
        final var preExistingData = snapshot.readNode(YangInstanceIdentifier.of());
        if (preExistingData.isEmpty()) {
            listener.onInitialData();
            return reg;
        }

        final var data = preExistingData.orElseThrow();
        if (treeId.isEmpty()) {
            if (data instanceof DataContainerNode container) {
                if (container.isEmpty()) {
                    // If we are listening on root of data tree we still get empty normalized node, root is always
                    // present, we should filter this out separately and notify it by 'onInitialData()' once.
                    // Otherwise, it is just a valid data node with empty value which also should be notified by
                    // "onDataTreeChanged(List<DataTreeCandidate>)".
                    listener.onInitialData();
                    return reg;
                }
            } else {
                throw new IllegalStateException("Unexpected root node type " + data.contract());
            }
        }

        final var candidate = DataTreeCandidates.fromNormalizedNode(YangInstanceIdentifier.of(), data);
        final var publisher = new InMemoryDOMStoreTreeChangePublisher(notificationManager);
        publisher.registerTreeChangeListener(treeId, listener);
        if (!publisher.publishChange(candidate)) {
            // There is no data in the conceptual data tree then notify with 'onInitialData()'.
            listener.onInitialData();
        }

        return reg;
    }

    synchronized boolean publishChange(final @NonNull DataTreeCandidate candidate) {
        // Runs synchronized with registrationRemoved()
        return processCandidateTree(candidate);
    }
}
