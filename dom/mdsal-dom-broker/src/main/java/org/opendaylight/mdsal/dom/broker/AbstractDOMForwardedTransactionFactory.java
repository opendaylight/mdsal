/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.util.concurrent.FluentFuture;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionFactory;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract composite transaction factory.
 *
 *<p>
 * Provides an convenience common implementation for composite DOM Transactions, where subtransaction is identified by
 * {@link LogicalDatastoreType} type and implementation of subtransaction is provided by
 * {@link DOMStoreTransactionFactory}.
 *
 * <b>Note:</b>This class does not have thread-safe implementation of  {@link #close()}, implementation may allow
 *             accessing and allocating new transactions during closing this instance.
 *
 * @param <T> Type of {@link DOMStoreTransactionFactory} factory.
 */
abstract class AbstractDOMForwardedTransactionFactory<T extends DOMStoreTransactionFactory> implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMForwardedTransactionFactory.class);
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup()
                .findVarHandle(AbstractDOMForwardedTransactionFactory.class, "closed", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Map<LogicalDatastoreType, T> storeTxFactories;

    private volatile boolean closed;

    protected AbstractDOMForwardedTransactionFactory(final Map<LogicalDatastoreType, ? extends T> txFactories) {
        this.storeTxFactories = new EnumMap<>(txFactories);
    }

    /**
     * Implementations must return unique identifier for each and every call of this method.
     *
     * @return new Unique transaction identifier.
     */
    protected abstract Object newTransactionIdentifier();

    /**
     * User-supplied implementation of {@link DOMDataTreeWriteTransaction#commit()} for transaction.
     *
     *<p>
     * Callback invoked when {@link DOMDataTreeWriteTransaction#commit()} is invoked on transaction created by this
     * factory.
     *
     * @param transaction Transaction on which {@link DOMDataTreeWriteTransaction#commit()} was invoked.
     * @param cohorts Iteratable of cohorts for subtransactions associated with the transaction being committed.
     * @return a FluentFuture. if commit coordination on cohorts finished successfully, a CommitInfo is returned from
     *         the Future, On failure, the Future fails with a {@link TransactionCommitFailedException}.
     */
    protected abstract FluentFuture<? extends CommitInfo> commit(DOMDataTreeWriteTransaction transaction,
            Collection<DOMStoreThreePhaseCommitCohort> cohorts);

    /**
     * Creates a new composite read-only transaction.
     *
     * <p>
     * Creates a new composite read-only transaction backed by one transaction per factory in {@link #getTxFactories()}.
     *
     * <p>
     * Subtransaction for reading is selected by supplied {@link LogicalDatastoreType} as parameter for
     * {@link DOMDataTreeReadTransaction#read(LogicalDatastoreType, YangInstanceIdentifier)}
     *
     * <p>
     * Id of returned transaction is retrieved via {@link #newTransactionIdentifier()}.
     *
     * @return New composite read-only transaction.
     */
    public final DOMDataTreeReadTransaction newReadOnlyTransaction() {
        checkNotClosed();

        final var txns = new EnumMap<LogicalDatastoreType, DOMStoreReadTransaction>(LogicalDatastoreType.class);
        for (var store : storeTxFactories.entrySet()) {
            txns.put(store.getKey(), store.getValue().newReadOnlyTransaction());
        }
        return new DOMForwardedReadOnlyTransaction(newTransactionIdentifier(), txns);
    }

    /**
     * Creates a new composite write-only transaction
     *
     * <p>
     * Creates a new composite write-only transaction backed by one write-only transaction per factory in
     * {@link #getTxFactories()}.
     *
     * <p>
     * Implementation of composite Write-only transaction is following:
     *
     * <ul>
     * <li>{@link DOMDataTreeWriteTransaction#put(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
     *     - backing subtransaction is selected by {@link LogicalDatastoreType},
     *       {@link DOMStoreWriteTransaction#write(YangInstanceIdentifier, NormalizedNode)} is invoked on selected
     *       subtransaction.</li>
     * <li> {@link DOMDataTreeWriteTransaction#merge(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
     *      - backing subtransaction is selected by {@link LogicalDatastoreType},
     *        {@link DOMStoreWriteTransaction#merge(YangInstanceIdentifier, NormalizedNode)} is invoked on selected
     *        subtransaction.</li>
     * <li>{@link DOMDataTreeWriteTransaction#delete(LogicalDatastoreType, YangInstanceIdentifier)}
     *     - backing subtransaction is selected by {@link LogicalDatastoreType},
     *       {@link DOMStoreWriteTransaction#delete(YangInstanceIdentifier)} is invoked on selected subtransaction.
     * <li>{@link DOMDataTreeWriteTransaction#commit()} - results in invoking {@link DOMStoreWriteTransaction#ready()},
     *     gathering all resulting cohorts and then invoking finalized implementation callback
     *     {@link #commit(DOMDataTreeWriteTransaction, Collection)} with transaction which was committed and gathered
     *     results.</li>
     * </ul>
     *
     * <p>
     * Id of returned transaction is generated via {@link #newTransactionIdentifier()}.
     *
     * @return New composite write-only transaction associated with this factory.
     */
    public final DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        checkNotClosed();

        final var txns = new EnumMap<LogicalDatastoreType, DOMStoreWriteTransaction>(LogicalDatastoreType.class);
        for (var store : storeTxFactories.entrySet()) {
            txns.put(store.getKey(), store.getValue().newWriteOnlyTransaction());
        }
        return new DOMForwardedWriteTransaction<>(newTransactionIdentifier(), txns, this);
    }

    /**
     * Creates a new composite read-write transaction.
     *
     * @return New composite read-write transaction associated with this factory.
     */
    public final DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        checkNotClosed();

        final var txns = new EnumMap<LogicalDatastoreType, DOMStoreReadWriteTransaction>(LogicalDatastoreType.class);
        for (var store : storeTxFactories.entrySet()) {
            txns.put(store.getKey(), store.getValue().newReadWriteTransaction());
        }
        return new DOMForwardedReadWriteTransaction(newTransactionIdentifier(), txns, this);
    }

    /**
     * Convenience accessor of backing factories intended to be used only by finalization of this class.
     *
     * <b>Note:</b> Finalization of this class may want to access other functionality of supplied Transaction factories.
     *
     * @return Map of backing transaction factories.
     */
    protected final Map<LogicalDatastoreType, T> getTxFactories() {
        return storeTxFactories;
    }

    /**
     * Checks if instance is not closed.
     *
     * @throws IllegalStateException If instance of this class was closed.
     */
    protected final void checkNotClosed() {
        checkState(!closed, "Transaction factory was closed. No further operations allowed.");
    }

    @Override
    public void close() {
        if (!CLOSED.compareAndSet(this, 0, 1)) {
            LOG.warn("Transaction factory was already closed", new Throwable());
        }
    }
}
