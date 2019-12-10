/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import java.util.EventListener;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedListNotification;

/**
 * @author nite
 *
 */
@Beta
public interface InstanceNotificationService extends BindingService {

    <P extends DataObject, N extends InstanceNotification<N, P>, T extends Listener<P, N>>
        @NonNull Registration registerListener(Class<N> type, LogicalDatastoreType datastore,
            InstanceIdentifier<P> path, T listener, Executor executor);

    <P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>, K extends Identifier<P>,
        T extends KeyedListListener<P, N, K>> @NonNull Registration registerListener(Class<N> type,
            LogicalDatastoreType datastore, KeyedInstanceIdentifier<P, K> path, T listener, Executor executor);

    /*
     * Interface for listeners on instance (YANG 1.1) notifications.
     */
    @FunctionalInterface
    interface Listener<P extends DataObject, N extends InstanceNotification<N, P>> extends EventListener {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        void onNotification(@NonNull LogicalDatastoreType datastore, @NonNull InstanceIdentifier<P> path,
            @NonNull N notification);
    }

    /**
     * Interface for listeners on instance (YANG 1.1) notifications defined in a {@code list} with a {@code key}.
     */
    @FunctionalInterface
    interface KeyedListListener<P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>,
            K extends Identifier<P>> extends EventListener {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        void onNotification(@NonNull LogicalDatastoreType datastore, @NonNull KeyedInstanceIdentifier<P, K> path,
            @NonNull N notification);
    }
}
