/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-Write Transaction, which is composed of several {@link DOMStoreWriteTransaction} transactions. A sub-transaction
 * is selected by {@link LogicalDatastoreType} type parameter in:
 * <ul>
 * <li>{@link #put(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
 * <li>{@link #delete(LogicalDatastoreType, YangInstanceIdentifier)}
 * <li>{@link #merge(LogicalDatastoreType, YangInstanceIdentifier, NormalizedNode)}
 * </ul>
 *
 * <p>
 * {@link #submit()} will result in invocation of
 * {@link DOMDataCommitImplementation#submit(org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction, Iterable)}
 * invocation with all {@link org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort} for underlying
 * transactions.
 *
 * @param <T> Subtype of {@link DOMStoreWriteTransaction} which is used as subtransaction.
 */
class DOMForwardedWriteTransaction<T extends DOMStoreWriteTransaction>
        extends AbstractDOMForwardedTransaction<T> implements DOMDataTreeWriteTransaction {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DOMForwardedWriteTransaction,
        AbstractDOMForwardedTransactionFactory> IMPL_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
                DOMForwardedWriteTransaction.class, AbstractDOMForwardedTransactionFactory.class, "commitImpl");
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DOMForwardedWriteTransaction, FluentFuture> FUTURE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DOMForwardedWriteTransaction.class, FluentFuture.class,
                "commitFuture");
    private static final Logger LOG = LoggerFactory.getLogger(DOMForwardedWriteTransaction.class);

    private final @NonNull SettableFuture<@NonNull CommitInfo> settableCompletion = SettableFuture.create();
    private final @NonNull FluentFuture<? extends @NonNull CommitInfo> completionFuture = FluentFuture.from(
        new UncancellableListenableFuture<>(settableCompletion));

    /*
     * Implementation of real commit. It also acts as an indication that the transaction is running -- which we flip
     * atomically using {@link #IMPL_UPDATER}.
     */
    private volatile AbstractDOMForwardedTransactionFactory<?> commitImpl;

    /*
     * Future task of transaction commit. It starts off as null, but is set appropriately on {@link #submit()} and
     * {@link #cancel()} via {@link AtomicReferenceFieldUpdater#lazySet(Object, Object)}.
     *
     * Lazy set is safe for use because it is only referenced to in the {@link #cancel()} slow path, where we will
     * busy-wait for it. The fast path gets the benefit of a store-store barrier instead of the usual store-load
     * barrier.
     */
    private volatile FluentFuture<?> commitFuture;

    protected DOMForwardedWriteTransaction(final Object identifier,
            final Function<LogicalDatastoreType, T> backingTxFactory,
            final AbstractDOMForwardedTransactionFactory<?> commitImpl) {
        super(identifier, backingTxFactory);
        this.commitImpl = requireNonNull(commitImpl, "commitImpl must not be null.");
    }

    @Override
    public final void put(final LogicalDatastoreType store, final YangInstanceIdentifier path,
            final NormalizedNode data) {
        checkRunning(commitImpl);
        getSubtransaction(store).write(path, data);
    }

    @Override
    public final void delete(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        checkRunning(commitImpl);
        getSubtransaction(store).delete(path);
    }

    @Override
    public final void merge(final LogicalDatastoreType store, final YangInstanceIdentifier path,
            final NormalizedNode data) {
        checkRunning(commitImpl);
        getSubtransaction(store).merge(path, data);
    }

    @Override
    public final boolean cancel() {
        final AbstractDOMForwardedTransactionFactory<?> impl = IMPL_UPDATER.getAndSet(this, null);
        if (impl != null) {
            LOG.trace("Transaction {} cancelled before submit", getIdentifier());
            FUTURE_UPDATER.lazySet(this, FluentFutures.immediateCancelledFluentFuture());
            closeSubtransactions();
            return true;
        }

        // The transaction is in process of being submitted or cancelled. Busy-wait for the corresponding future.
        FluentFuture<?> future;
        do {
            future = commitFuture;
        } while (future == null);

        return future.isCancelled();
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final FluentFuture<? extends CommitInfo> commit() {
        final AbstractDOMForwardedTransactionFactory<?> impl = IMPL_UPDATER.getAndSet(this, null);
        checkRunning(impl);

        FluentFuture<? extends CommitInfo> ret;
        final var tx = getSubtransaction();
        if (tx == null) {
            ret = CommitInfo.emptyFluentFuture();
        } else {
            try {
                ret = impl.commit(this, tx.ready());
            } catch (RuntimeException e) {
                ret = immediateFailedFluentFuture(TransactionCommitFailedExceptionMapper.COMMIT_ERROR_MAPPER.apply(e));
            }
        }

        settableCompletion.setFuture(ret);
        FUTURE_UPDATER.lazySet(this, ret);
        return completionFuture;
    }

    @Override
    public final FluentFuture<?> completionFuture() {
        return completionFuture;
    }

    private void checkRunning(final AbstractDOMForwardedTransactionFactory<?> impl) {
        checkState(impl != null, "Transaction %s is no longer running", getIdentifier());
    }
}
