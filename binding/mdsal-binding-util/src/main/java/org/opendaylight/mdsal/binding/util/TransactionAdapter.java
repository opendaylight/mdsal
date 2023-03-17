/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Adapter allowing managed, datastore-constrained transactions to be used with methods expecting
 * generic {@link DataBroker} transactions.
 *
 * <p>The adapted transactions maintain the following constraints: they cannot be cancelled or
 * submitted (only the transaction manager can do this), and they cannot access a logical datastore
 * other than the one they were created for.
 *
 * @deprecated This is only intended for temporary use during complex migrations to managed transactions.
 */
@Deprecated
public final class TransactionAdapter {
    private TransactionAdapter() {
    }

    /**
     * Adapts the given datastore-constrained read-write transaction to a generic read-write transaction.
     *
     * @param datastoreTx The transaction to adapt.
     * @return The adapted transaction.
     * @throws NullPointerException if the provided transaction is {@code null}.
     */
    public static ReadWriteTransaction toReadWriteTransaction(
            final TypedReadWriteTransaction<? extends Datastore> datastoreTx) {
        if (datastoreTx instanceof TypedReadWriteTransactionImpl) {
            final TypedReadWriteTransactionImpl<?> txImpl = (TypedReadWriteTransactionImpl<?>) datastoreTx;
            return new ReadWriteTransactionAdapter<>(txImpl.getDatastoreType(), txImpl);
        }
        throw new IllegalArgumentException("Unsupported TypedWriteTransaction implementation "
                + datastoreTx.getClass());
    }

    /**
     * Adapts the given datastore-constrained write transaction to a generic write transaction. Note that this
     * can be used to adapt a read-write transaction to a write transaction.
     *
     * @param datastoreTx The transaction to adapt.
     * @return The adapted transaction.
     */
    public static WriteTransaction toWriteTransaction(final TypedWriteTransaction<? extends Datastore> datastoreTx) {
        if (datastoreTx instanceof TypedWriteTransactionImpl) {
            final TypedWriteTransactionImpl<?, ?> txImpl = (TypedWriteTransactionImpl<?, ?>) datastoreTx;
            return new WriteTransactionAdapter<>(txImpl.getDatastoreType(), txImpl);
        }
        throw new IllegalArgumentException("Unsupported TypedWriteTransaction implementation "
                + datastoreTx.getClass());
    }

    private static class WriteTransactionAdapter<S extends Datastore, D extends TypedWriteTransaction<S>>
            extends ForwardingObject implements WriteTransaction {
        private final LogicalDatastoreType datastoreType;
        private final D delegate;

        WriteTransactionAdapter(final LogicalDatastoreType datastoreType, final D delegate) {
            this.datastoreType = datastoreType;
            this.delegate = delegate;
        }

        @Override
        public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
                final T data) {
            checkStore(store);
            delegate.put(path, data);
        }

        @Override
        public <T extends DataObject> void put(@NonNull LogicalDatastoreType store,
                org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path,
                @NonNull T data) {
            checkStore(store);
            delegate.put(path,data);
        }

        @Override
        public <T extends DataObject> void mergeParentStructurePut(final LogicalDatastoreType store,
                final InstanceIdentifier<T> path, final T data) {
            checkStore(store);
            delegate.mergeParentStructurePut(path, data);
        }

        @Override
        public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
                final T data) {
            checkStore(store);
            delegate.merge(path, data);
        }

        @Override
        public <T extends DataObject> void merge(@NonNull LogicalDatastoreType store,
                org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path,
                @NonNull T data) {
            checkStore(store);
            delegate.merge(path, data);
        }

        @Override
        public <T extends DataObject> void mergeParentStructureMerge(final LogicalDatastoreType store,
                final InstanceIdentifier<T> path, final T data) {
            checkStore(store);
            delegate.mergeParentStructureMerge(path, data);
        }

        @Override
        public boolean cancel() {
            throw new UnsupportedOperationException("Managed transactions must not be cancelled");
        }

        @Override
        public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
            checkStore(store);
            delegate.delete(path);
        }

        @Override
        public void delete(@NonNull LogicalDatastoreType store,
                org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<?> path) {
            checkStore(store);
            delegate.delete(path);
        }

        @Override
        public FluentFuture<? extends CommitInfo> commit() {
            throw new UnsupportedOperationException("Managed transactions must not be committed");
        }

        @Override
        public Object getIdentifier() {
            return delegate.getIdentifier();
        }

        @Override
        protected D delegate() {
            return delegate;
        }

        void checkStore(final LogicalDatastoreType store) {
            checkArgument(datastoreType.equals(store), "Invalid datastore %s used instead of %s", store, datastoreType);
        }
    }

    private static final class ReadWriteTransactionAdapter<S extends Datastore>
            extends WriteTransactionAdapter<S, TypedReadWriteTransaction<S>> implements ReadWriteTransaction {
        ReadWriteTransactionAdapter(final LogicalDatastoreType datastoreType,
                final TypedReadWriteTransaction<S> delegate) {
            super(datastoreType, delegate);
        }

        @Override
        public <T extends DataObject> FluentFuture<Optional<T>> read(final LogicalDatastoreType store,
                final InstanceIdentifier<T> path) {
            checkStore(store);
            return delegate().read(path);
        }

        @Override
        public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(
                @NonNull LogicalDatastoreType store,
                org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path) {
            checkStore(store);
            return delegate().read(path);
        }

        @Override
        public FluentFuture<Boolean> exists(final LogicalDatastoreType store,final InstanceIdentifier<?> path) {
            checkStore(store);
            return delegate().exists(path);
        }

        @Override
        public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store,
                org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<?> path) {
            return delegate().exists(path);
        }
    }
}
