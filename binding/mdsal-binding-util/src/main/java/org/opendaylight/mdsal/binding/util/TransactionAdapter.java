/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Adapter allowing managed, datastore-constrained transactions to be used with methods expecting
 * generic {@link DataBroker} transactions.
 *
 * <p>The adapted transactions maintain the following constraints: they cannot be cancelled or
 * submitted (only the transaction manager can do this), and they cannot access a logical datastore
 * other than the one they were created for.
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
     */
    public static ReadWriteTransaction toReadWriteTransaction(
            TypedReadWriteTransaction<? extends Datastore> datastoreTx) {
        if (datastoreTx instanceof TypedReadWriteTransactionImpl) {
            TypedReadWriteTransactionImpl nonSubmitCancelableDatastoreReadWriteTransaction =
                    (TypedReadWriteTransactionImpl) datastoreTx;
            return new ReadWriteTransactionAdapter(nonSubmitCancelableDatastoreReadWriteTransaction.datastoreType,
                    nonSubmitCancelableDatastoreReadWriteTransaction);
        }
        throw new IllegalArgumentException(
                "Unsupported TypedWriteTransaction implementation " + datastoreTx.getClass());
    }

    /**
     * Adapts the given datastore-constrained write transaction to a generic write transaction. Note that this
     * can be used to adapt a read-write transaction to a write transaction.
     *
     * @param datastoreTx The transaction to adapt.
     * @return The adapted transaction.
     */
    public static WriteTransaction toWriteTransaction(
        TypedWriteTransaction<? extends Datastore> datastoreTx) {
        if (datastoreTx instanceof TypedWriteTransactionImpl) {
            TypedWriteTransactionImpl nonSubmitCancelableDatastoreWriteTransaction =
                    (TypedWriteTransactionImpl) datastoreTx;
            return new WriteTransactionAdapter(nonSubmitCancelableDatastoreWriteTransaction.datastoreType,
                    nonSubmitCancelableDatastoreWriteTransaction);
        }
        throw new IllegalArgumentException(
                "Unsupported TypedWriteTransaction implementation " + datastoreTx.getClass());
    }

    // We want to subclass this class, even though it has a private constructor
    @SuppressWarnings("FinalClass")
    private static class WriteTransactionAdapter<D extends Datastore> implements WriteTransaction {
        final LogicalDatastoreType datastoreType;
        final TypedWriteTransaction<D> delegate;

        private WriteTransactionAdapter(LogicalDatastoreType datastoreType, TypedWriteTransaction<D> delegate) {
            this.datastoreType = datastoreType;
            this.delegate = delegate;
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
            checkStore(store);
            delegate.put(path, data);
        }

        @Override
        public <T extends DataObject> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
                boolean createMissingParents) {
            checkStore(store);
            delegate.put(path, data, createMissingParents);
        }

        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data) {
            checkStore(store);
            delegate.merge(path, data);
        }

        @Override
        public <T extends DataObject> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
                boolean createMissingParents) {
            checkStore(store);
            delegate.merge(path, data, createMissingParents);
        }

        @Override
        public boolean cancel() {
            throw new UnsupportedOperationException("Managed transactions mustn't be cancelled");
        }

        @Override
        public void delete(LogicalDatastoreType store, InstanceIdentifier<?> path) {
            checkStore(store);
            delegate.delete(path);
        }

        @Override
        public @NonNull FluentFuture<? extends CommitInfo> commit() {
            throw new UnsupportedOperationException("Managed transactions mustn't be committed");
        }

        void checkStore(LogicalDatastoreType store) {
            Preconditions.checkArgument(datastoreType.equals(store), "Invalid datastore %s used instead of %s", store,
                datastoreType);
        }

        @Override
        public Object getIdentifier() {
            return delegate.getIdentifier();
        }
    }

    private static final class ReadWriteTransactionAdapter<D extends Datastore> extends WriteTransactionAdapter<D>
            implements ReadWriteTransaction {
        private final TypedReadWriteTransaction<D> delegate;

        private ReadWriteTransactionAdapter(LogicalDatastoreType datastoreType, TypedReadWriteTransaction<D> delegate) {
            super(datastoreType, delegate);
            this.delegate = delegate;
        }

        @Override
        public <T extends DataObject> FluentFuture<Optional<T>> read(LogicalDatastoreType store,
                InstanceIdentifier<T> path) {
            checkStore(store);
            return delegate.read(path);
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }
}
