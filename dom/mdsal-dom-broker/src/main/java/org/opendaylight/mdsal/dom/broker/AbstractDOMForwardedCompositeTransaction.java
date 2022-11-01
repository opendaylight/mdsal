/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransaction;

/**
 * Composite DOM Transaction backed by {@link DOMStoreTransaction}.
 *
 * <p>
 * Abstract base for composite transaction, which provides access only to common
 * functionality as retrieval of subtransaction, close method and retrieval of
 * identifier.
 *
 * @param <T> Subtransaction type
 */
abstract class AbstractDOMForwardedCompositeTransaction<T extends DOMStoreTransaction>
        implements DOMDataTreeTransaction {
    private static final VarHandle BACKING_TX;

    static {
        try {
            BACKING_TX = MethodHandles.lookup().findVarHandle(AbstractDOMForwardedCompositeTransaction.class,
                "backingTx", Entry.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Object identifier;
    private final Function<LogicalDatastoreType, T> backingTxFactory;

    private volatile Entry<LogicalDatastoreType, T> backingTx;

    /**
     * Creates new composite Transactions.
     *
     * @param identifier
     *            Identifier of transaction.
     * @param backingTxs
     *            Key,value map of backing transactions.
     */
    protected AbstractDOMForwardedCompositeTransaction(final Object identifier,
            final Function<LogicalDatastoreType, T> backingTxFactory) {
        this.identifier = requireNonNull(identifier, "Identifier should not be null");
        this.backingTxFactory = requireNonNull(backingTxFactory, "Backing transactions should not be null");
    }

    /**
     * Returns subtransaction associated with supplied datastore type.
     *
     * <p>
     * The method allows usage of single datastore type per transaction instance;
     * eligible datastore type is defined by first method access.
     *
     * @param datastoreType is used to identify subtransaction object
     * @return the subtransaction object
     * @throws NullPointerException     if datastoreType is null
     * @throws IllegalArgumentException if no ubtransaction is associated with datastoreType.
     * @throws TransactionDatastoreMismatchException if datastoreType mismatches the one used at first access
     */
    protected final T getSubtransaction(final LogicalDatastoreType datastoreType) {
        final var ds = requireNonNull(datastoreType, "datastoreType must not be null.");

        var entry = backingTx;
        if (entry == null) {
            final var tx = backingTxFactory.apply(datastoreType);
            final var newEntry = Map.entry(ds, tx);
            final var witness = (Entry<LogicalDatastoreType, T>) BACKING_TX.compareAndExchange(this, null, newEntry);
            if (witness != null) {
                tx.close();
                entry = witness;
            } else {
                entry = witness;
            }
        }

        final var encountered = entry.getKey();
        if (encountered != datastoreType) {
            throw new TransactionDatastoreMismatchException(ds, encountered);
        }
        return entry.getValue();
    }

    /**
     * Returns immutable Iterable of all subtransactions.
     */
    protected @Nullable T getSubtransaction() {
        final Entry<LogicalDatastoreType, T> entry;
        return (entry = backingTx) == null ? null : entry.getValue();
    }

    @Override
    public Object getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void closeSubtransactions() {
        /*
         * We share one exception for all failures, which are added
         * as supressedExceptions to it.
         */
        final var subtransaction = getSubtransaction();
        if (subtransaction != null) {
            try {
                subtransaction.close();
            } catch (Exception e) {
                // If we did not allocate failure we allocate it
                throw new IllegalStateException("Uncaught exception occurred during closing transaction", e);
            }
        }
    }
}
