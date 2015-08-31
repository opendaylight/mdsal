/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

class ShardedDOMDataTreeListenerContext<T extends DOMDataTreeListener> implements AutoCloseable {

    private final DOMDataTreeListener listener;
    private final EnumMap<LogicalDatastoreType, StoreListener> storeListeners = new EnumMap<>(
            LogicalDatastoreType.class);
    private final Collection<ListenerRegistration<?>> registrations = new ArrayList<>();

    // FIXME: Probably should be encapsulated into state object
    @GuardedBy("this")
    private Collection<DataTreeCandidate> unreported = new ArrayList<>();
    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> currentData = Collections.emptyMap();

    private ShardedDOMDataTreeListenerContext(T listener, Collection<DOMDataTreeIdentifier> subtrees,
            boolean allowRxMerges) {
        for (LogicalDatastoreType type : LogicalDatastoreType.values()) {
            storeListeners.put(type, new StoreListener(type));
        }
        this.listener = Preconditions.checkNotNull(listener, "listener");
    }

    static <T extends DOMDataTreeListener> ShardedDOMDataTreeListenerContext<T> create(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges) {
        return new ShardedDOMDataTreeListenerContext<>(listener, subtrees, allowRxMerges);
    }

    synchronized void notifyListener() {
        Collection<DataTreeCandidate> changesToNotify = unreported;
        unreported = new ArrayList<>();
        listener.onDataTreeChanged(changesToNotify, currentData);

    }

    void register(DOMDataTreeIdentifier subtree, DOMStoreTreeChangePublisher shard) {
        ListenerRegistration<?> storeReg =
                shard.registerTreeChangeListener(subtree.getRootIdentifier(),
                        storeListeners.get(subtree.getDatastoreType()));
        registrations.add(storeReg);
    }

    private final class StoreListener implements DOMDataTreeChangeListener {

        private final LogicalDatastoreType type;

        public StoreListener(LogicalDatastoreType type) {
            this.type = type;
        }

        @Override
        public void onDataTreeChanged(Collection<DataTreeCandidate> changes) {
            receivedDataTreeChanges(type, changes);
            scheduleNotification();
        }

    }

    // FIXME: Should be able to run parallel to notifyListener and should honor
    // allowRxMerges
    synchronized void receivedDataTreeChanges(LogicalDatastoreType type, Collection<DataTreeCandidate> changes) {
        Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> updatedData =
                MapAdaptor.getDefaultInstance().takeSnapshot(currentData);
        for (DataTreeCandidate change : changes) {
            // FIXME: Make sure only one is reported / merged
            unreported.add(change);
            DOMDataTreeIdentifier treeId = new DOMDataTreeIdentifier(type, change.getRootPath());
            // FIXME: Probably we should apply data tree candidate to previously observed state
            Optional<NormalizedNode<?, ?>> dataAfter = change.getRootNode().getDataAfter();
            if (dataAfter.isPresent()) {
                updatedData.put(treeId, dataAfter.get());
            } else {
                updatedData.remove(treeId);
            }
        }
        currentData = MapAdaptor.getDefaultInstance().optimize(updatedData);
    }

    void scheduleNotification() {
        // FIXME: This callout should schedule delivery task
        notifyListener();
    }

    @Override
    public void close() {
        for (ListenerRegistration<?> reg : registrations) {
            reg.close();
        }
    }
}
