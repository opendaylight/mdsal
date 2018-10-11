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
    public static WriteTransaction toWriteTransaction(final TypedWriteTransaction<? extends Datastore> datastoreTx) {
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
    private static class WriteTransactionAdapter<D extends Datastore, T extends TypedWriteTransaction<D>>
            extends ForwardingObject implements WriteTransaction {
        private final LogicalDatastoreType datastoreType;
        private final T delegate;

        private WriteTransactionAdapter(final LogicalDatastoreType datastoreType, final T delegate) {
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
        public <T extends DataObject> void put(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
                final T data, final boolean createMissingParents) {
            checkStore(store);
            delegate.put(path, data, createMissingParents);
        }

        @Override
        public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
                final T data) {
            checkStore(store);
            delegate.merge(path, data);
        }

        @Override
        public <T extends DataObject> void merge(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
                final T data, final boolean createMissingParents) {
            checkStore(store);
            delegate.merge(path, data, createMissingParents);
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
        public @NonNull FluentFuture<? extends CommitInfo> commit() {
            throw new UnsupportedOperationException("Managed transactions must not be committed");
        }

        void checkStore(final LogicalDatastoreType store) {
            checkArgument(datastoreType.equals(store), "Invalid datastore %s used instead of %s", store, datastoreType);
        }

        @Override
        public Object getIdentifier() {
            return delegate.getIdentifier();
        }

        @Override
        protected T delegate() {
            return delegate;
        }
    }

    private static final class ReadWriteTransactionAdapter<D extends Datastore>
            extends WriteTransactionAdapter<D, TypedReadWriteTransaction<D>> implements ReadWriteTransaction {
        private ReadWriteTransactionAdapter(final LogicalDatastoreType datastoreType,
                final TypedReadWriteTransaction<D> delegate) {
            super(datastoreType, delegate);
        }

        @Override
        public <T extends DataObject> FluentFuture<Optional<T>> read(final LogicalDatastoreType store,
                final InstanceIdentifier<T> path) {
            checkStore(store);
            return delegate().read(path);
        }
    }
}
