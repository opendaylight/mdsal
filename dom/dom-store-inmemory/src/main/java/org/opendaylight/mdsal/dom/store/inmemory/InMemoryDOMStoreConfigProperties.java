/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Generated;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.opendaylight.mdsal.dom.store.inmemory.impl.InMemoryDOMStoreImpl;

/**
 * Holds configuration properties when creating an {@link InMemoryDOMStoreImpl} instance via the
 * {@link InMemoryDOMStoreFactory}.
 *
 * @author Thomas Pantelis
 * @see InMemoryDOMStoreFactory
 */
@Value.Immutable
@Value.Style(
    visibility = ImplementationVisibility.PRIVATE,
    allowedClasspathAnnotations = { Generated.class, SuppressWarnings.class })
@NonNullByDefault
public abstract class InMemoryDOMStoreConfigProperties {
    private static final class Holder {
        static final InMemoryDOMStoreConfigProperties DEFAULT = builder().build();

        private Holder() {
            // hidden on purpose
        }
    }

    public static final int DEFAULT_MAX_DATA_CHANGE_EXECUTOR_QUEUE_SIZE = 1000;
    public static final int DEFAULT_MAX_DATA_CHANGE_EXECUTOR_POOL_SIZE = 20;
    public static final int DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE = 1000;
    public static final int DEFAULT_MAX_DATA_STORE_EXECUTOR_QUEUE_SIZE = 5000;

    InMemoryDOMStoreConfigProperties() {
        // hidden on purpose
    }

    /**
     * {@return the InMemoryDOMDataStoreConfigProperties instance with default values}
     */
    public static final InMemoryDOMStoreConfigProperties getDefault() {
        return Holder.DEFAULT;
    }

    /**
     * {@return a new {@link InMemoryDOMStoreConfigPropertiesBuilder}}
     */
    public static final InMemoryDOMStoreConfigPropertiesBuilder builder() {
        return new InMemoryDOMStoreConfigPropertiesBuilder();
    }

    /**
     * {@return true if transaction allocation debugging should be enabled}
     */
    @Value.Default
    public boolean getDebugTransactions() {
        return false;
    }

    /**
     * {@return the maximum queue size for the data change notification executor}
     */
    @Value.Default
    public int getMaxDataChangeExecutorQueueSize() {
        return DEFAULT_MAX_DATA_CHANGE_EXECUTOR_QUEUE_SIZE;
    }

    /**
     * {@return the maximum thread pool size for the data change notification executor}
     */
    @Value.Default
    public int getMaxDataChangeExecutorPoolSize() {
        return DEFAULT_MAX_DATA_CHANGE_EXECUTOR_POOL_SIZE;
    }

    /**
     * {@return the maximum queue size for the data change listeners}
     */
    @Value.Default
    public int getMaxDataChangeListenerQueueSize() {
        return DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE;
    }

    /**
     * {@return the maximum queue size for the data store executor}
     */
    @Value.Default
    public int getMaxDataStoreExecutorQueueSize() {
        return DEFAULT_MAX_DATA_STORE_EXECUTOR_QUEUE_SIZE;
    }
}
