/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationListener;
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tree of {@link DOMInstanceNotificationListener}s associated with a particular notification type on a particular
 * data store.
 */
final class InstanceNotificationListeners extends AbstractRegistrationTree<InstanceNotificationListeners.Reg> {
    @NonNullByDefault
    sealed interface Reg permits RegImpl {
        /**
         * Return the underlying listener.
         *
         * @return the underlying listener
         */
        DOMInstanceNotificationListener listener();

        /**
         * Check if this handle has not been closed yet.
         *
         * @return {@code true} if this handle is still open
         */
        boolean notClosed();
    }

    @NonNullByDefault
    private final class RegImpl extends AbstractObjectRegistration<DOMInstanceNotificationListener> implements Reg {
        RegImpl(final DOMInstanceNotificationListener instance) {
            super(instance);
        }

        @Override
        public DOMInstanceNotificationListener listener() {
            return getInstance();
        }

        @Override
        protected void removeRegistration() {
            registrationRemoved(this);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InstanceNotificationListeners.class);

    @NonNullByDefault
    Registration registerListener(final YangInstanceIdentifier path, final Set<QName> notifications,
            final DOMInstanceNotificationListener listener) {
        // Take the write lock
        takeLock();
        try {
            final var reg = new RegImpl(listener);
            addRegistration(findNodeFor(path.getPathArguments()), reg);
            return reg;
        } finally {
            // Always release the lock
            releaseLock();
        }
    }

    void notifyListener(@NonNull Reg registration, @NonNull List<DataTreeCandidate> changes) {

    }

    private void registrationRemoved(final @NonNull RegImpl registration) {

    }
}
