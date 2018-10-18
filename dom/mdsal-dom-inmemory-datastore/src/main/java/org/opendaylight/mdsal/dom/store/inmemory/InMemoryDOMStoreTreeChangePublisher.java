/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDOMStoreTreeChangePublisher extends AbstractDOMStoreTreeChangePublisher {
    private static final BatchedInvoker<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate>
        MANAGER_INVOKER = (listener, notifications) -> {
            final DOMDataTreeChangeListener inst = listener.getInstance();
            if (inst != null) {
                inst.onDataTreeChanged(ImmutableList.copyOf(notifications));
            }
        };
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMStoreTreeChangePublisher.class);

    private final QueuedNotificationManager<AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate>
        notificationManager;

    InMemoryDOMStoreTreeChangePublisher(final ExecutorService listenerExecutor, final int maxQueueSize) {
        notificationManager = QueuedNotificationManager.create(listenerExecutor, MANAGER_INVOKER, maxQueueSize,
                "DataTreeChangeListenerQueueMgr");
    }

    private InMemoryDOMStoreTreeChangePublisher(final QueuedNotificationManager<
            AbstractDOMDataTreeChangeListenerRegistration<?>, DataTreeCandidate> notificationManager) {
        this.notificationManager = notificationManager;
    }

    QueuedNotificationManager<?, ?> getNotificationManager() {
        return notificationManager;
    }

    @Override
    protected void notifyListener(final AbstractDOMDataTreeChangeListenerRegistration<?> registration,
            final Collection<DataTreeCandidate> changes) {
        LOG.debug("Enqueueing candidates {} for registration {}", changes, registration);
        notificationManager.submitNotifications(registration, changes);
    }

    @Override
    protected synchronized void registrationRemoved(
            final AbstractDOMDataTreeChangeListenerRegistration<?> registration) {
        LOG.debug("Closing registration {}", registration);

        // FIXME: remove the queue for this registration and make sure we clear it
    }


    <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerTreeChangeListener(
            final YangInstanceIdentifier treeId, final L listener, final DataTreeSnapshot snapshot) {
        final AbstractDOMDataTreeChangeListenerRegistration<L> reg = registerTreeChangeListener(treeId, listener);

        final Optional<NormalizedNode<?, ?>> preExistingData = snapshot.readNode(YangInstanceIdentifier.EMPTY);
        final NormalizedNode<?, ?> data = preExistingData.get();

        if (YangInstanceIdentifier.EMPTY.equals(treeId)) {
            checkState(data instanceof DataContainerNode, "DataContainerNode is expected for the root.");
            if (((DataContainerNode) data).getValue().isEmpty()) {
                // If we are listening on root of data tree we still get
                // empty normalized node, root is always present, we should
                // filter this out separately and notify it by 'onInitialData()' once.
                // Otherwise, it's just a valid data node with empty value which also
                // should be notified by "onDataTreeChanged(Collection<DataTreeCandidate>)".
                listener.onInitialData();
                return reg;
            }
        }

        final DataTreeCandidate candidate = DataTreeCandidates.fromNormalizedNode(
                YangInstanceIdentifier.EMPTY, data);

        InMemoryDOMStoreTreeChangePublisher publisher =
                new InMemoryDOMStoreTreeChangePublisher(notificationManager);
        publisher.registerTreeChangeListener(treeId, listener);
        if (!publisher.publishChange(candidate)) {
            // There is no data in the conceptual data tree then
            // notify with 'onInitialData()'.
            listener.onInitialData();
        }

        return reg;
    }

    /*
    <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerTreeChangeListener(
            final YangInstanceIdentifier treeId, final L listener, final DataTreeSnapshot snapshot) {
        final AbstractDOMDataTreeChangeListenerRegistration<L> reg = registerTreeChangeListener(treeId, listener);

        final Optional<NormalizedNode<?, ?>> preExistingData = snapshot.readNode(treeId);
        final DataTreeCandidate initialCandidate;

        if (preExistingData.isPresent()) {
            final NormalizedNode<?, ?> data = preExistingData.get();

            // if we are listening on root of data tree we still get
            // empty normalized node, root is always present
            if (YangInstanceIdentifier.EMPTY.equals(treeId) && (data instanceof DataContainerNode)
                    && ((DataContainerNode<?>) data).getValue().isEmpty()) {
                listener.onInitialData();
            } else {
                initialCandidate = DataTreeCandidates.fromNormalizedNode(treeId, preExistingData.get());
                listener.onDataTreeChanged(Collections.singleton(initialCandidate));
            }
        } else {
            listener.onInitialData();
        }

        return reg;
    }
*/
    synchronized boolean publishChange(final @NonNull DataTreeCandidate candidate) {
        // Runs synchronized with registrationRemoved()
        return processCandidateTree(candidate);
    }
}
