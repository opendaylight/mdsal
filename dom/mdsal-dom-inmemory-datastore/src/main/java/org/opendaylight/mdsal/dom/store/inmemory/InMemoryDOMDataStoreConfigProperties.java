/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.eclipse.jdt.annotation.NonNull;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

/**
 * Holds configuration properties when creating an {@link InMemoryDOMDataStore} instance via the
 * {@link InMemoryDOMDataStoreFactory}.
 *
 * @author Thomas Pantelis
 * @see InMemoryDOMDataStoreFactory
 */
@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PRIVATE)
public abstract class InMemoryDOMDataStoreConfigProperties {

    public static final int DEFAULT_MAX_DATA_CHANGE_EXECUTOR_QUEUE_SIZE = 1000;
    public static final int DEFAULT_MAX_DATA_CHANGE_EXECUTOR_POOL_SIZE = 20;
    public static final int DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE = 1000;
    public static final int DEFAULT_MAX_DATA_STORE_EXECUTOR_QUEUE_SIZE = 5000;

    private static final @NonNull InMemoryDOMDataStoreConfigProperties DEFAULT = builder().build();

    /**
     * Returns the InMemoryDOMDataStoreConfigProperties instance with default values.
     *
     * @return the InMemoryDOMDataStoreConfigProperties instance with default values.
     */
    public static @NonNull InMemoryDOMDataStoreConfigProperties getDefault() {
        return DEFAULT;
    }

    /**
     * Returns a new {@link InMemoryDOMDataStoreConfigPropertiesBuilder}.
     *
     * @return a new {@link InMemoryDOMDataStoreConfigPropertiesBuilder}.
     */
    public static @NonNull InMemoryDOMDataStoreConfigPropertiesBuilder builder() {
        return new InMemoryDOMDataStoreConfigPropertiesBuilder();
    }

    /**
     * Returns true if transaction allocation debugging should be enabled.
     *
     * @return true if transaction allocation debugging should be enabled.
     */
    @Value.Default
    public boolean getDebugTransactions() {
        return false;
    }

    /**
     * Returns the maximum queue size for the data change notification executor.
     *
     * @return the maximum queue size for the data change notification executor.
     */
    @Value.Default
    public int getMaxDataChangeExecutorQueueSize() {
        return DEFAULT_MAX_DATA_CHANGE_EXECUTOR_QUEUE_SIZE;
    }

    /**
     * Returns the maximum thread pool size for the data change notification executor.
     *
     * @return the maximum thread pool size for the data change notification executor.
     */
    @Value.Default
    public int getMaxDataChangeExecutorPoolSize() {
        return DEFAULT_MAX_DATA_CHANGE_EXECUTOR_POOL_SIZE;
    }

    /**
     * Returns the maximum queue size for the data change listeners.
     */
    @Value.Default
    public int getMaxDataChangeListenerQueueSize() {
        return DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE;
    }

    /**
     * Returns the maximum queue size for the data store executor.
     */
    @Value.Default
    public int getMaxDataStoreExecutorQueueSize() {
        return DEFAULT_MAX_DATA_STORE_EXECUTOR_QUEUE_SIZE;
    }
}
