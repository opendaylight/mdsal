/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ExecutorServiceUtil;
import org.opendaylight.yangtools.util.concurrent.EqualityQueuedNotificationManager;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemoryDOMStoreTreeChangePublisher extends TreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMStoreTreeChangePublisher.class);

    // Registrations use identity for equality, hence we can skip wrapping them
    private final EqualityQueuedNotificationManager<Reg, DataTreeCandidate> notificationManager;
    private final @NonNull ExecutorService executor;

    InMemoryDOMStoreTreeChangePublisher(final String dsName, final ExecutorService executor, final int maxQueueSize) {
        this.executor = requireNonNull(executor);
        notificationManager = new EqualityQueuedNotificationManager<>("InMemoryDOMStoreTreeChangePublisher" + dsName,
            executor, maxQueueSize,
            (listener, notifications) -> {
                if (listener.notClosed()) {
                    listener.listener().onDataTreeChanged(notifications);
                }
            });
    }

    @Override
    protected void notifyListener(final Reg registration, final List<DataTreeCandidate> changes) {
        LOG.debug("Enqueueing candidates {} for registration {}", changes, registration);
        notificationManager.submitNotifications(registration, changes);
    }

    @Override
    void close() {
        ExecutorServiceUtil.tryGracefulShutdown(executor, 30, TimeUnit.SECONDS);
    }
}
