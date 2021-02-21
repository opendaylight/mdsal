/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
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
    private static final VarHandle DSTYPE;

    static {
        try {
            DSTYPE = MethodHandles.lookup().findVarHandle(AbstractDOMForwardedCompositeTransaction.class, "dsType",
                LogicalDatastoreType.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Map<LogicalDatastoreType, T> backingTxs;
    private final @NonNull Object identifier;

    @SuppressWarnings("unused")
    private volatile LogicalDatastoreType dsType;

    /**
     * Creates new composite Transactions.
     *
     * @param identifier
     *            Identifier of transaction.
     * @param backingTxs
     *            Key,value map of backing transactions.
     */
    protected AbstractDOMForwardedCompositeTransaction(final Object identifier,
            final Map<LogicalDatastoreType, T> backingTxs) {
        this.identifier = requireNonNull(identifier, "Identifier should not be null");
        this.backingTxs = requireNonNull(backingTxs, "Backing transactions should not be null");
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

        final var witness = (LogicalDatastoreType) DSTYPE.compareAndExchange(this, null, ds);
        if (witness != null && witness != ds) {
            throw new TransactionDatastoreMismatchException(ds, witness);
        }

        final T ret = backingTxs.get(datastoreType);
        checkArgument(ret != null, "No subtransaction associated with %s", datastoreType);
        return ret;
    }

    /**
     * Returns immutable Iterable of all subtransactions.
     */
    protected Collection<T> getSubtransactions() {
        return backingTxs.values();
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
        IllegalStateException failure = null;
        for (T subtransaction : backingTxs.values()) {
            try {
                subtransaction.close();
            } catch (Exception e) {
                // If we did not allocate failure we allocate it
                if (failure == null) {
                    failure = new IllegalStateException("Uncaught exception occurred during closing transaction", e);
                } else {
                    // We update it with additional exceptions, which occurred during error.
                    failure.addSuppressed(e);
                }
            }
        }
        // If we have failure, we throw it at after all attempts to close.
        if (failure != null) {
            throw failure;
        }
    }
}
