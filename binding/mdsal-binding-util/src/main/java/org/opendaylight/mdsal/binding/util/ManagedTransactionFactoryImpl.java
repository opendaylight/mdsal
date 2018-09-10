/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionFactory;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a {@link ManagedTransactionFactory}.
 */
class ManagedTransactionFactoryImpl<T extends TransactionFactory> implements ManagedTransactionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ManagedTransactionFactoryImpl.class);

    private final T transactionFactory;

    ManagedTransactionFactoryImpl(T transactionFactory) {
        this.transactionFactory = requireNonNull(transactionFactory, "transactionFactory must not be null");
    }

    @Override
    public <D extends Datastore, E extends Exception, R> R applyInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadTransaction<D>, R, E> txFunction)
            throws E, InterruptedException {
        try (ReadTransaction realTx = transactionFactory.newReadOnlyTransaction()) {
            TypedReadTransaction<D>
                wrappedTx = new TypedReadTransactionImpl<>(datastoreType, realTx);
            return txFunction.apply(wrappedTx);
        }
    }

    @Override
    public <D extends Datastore, E extends Exception, R> R applyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, CheckedFunction<TypedReadTransaction<D>, R, E> txFunction) throws E {
        try (ReadTransaction realTx = transactionFactory.newReadOnlyTransaction()) {
            TypedReadTransaction<D>
                wrappedTx = new TypedReadTransactionImpl<>(datastoreType, realTx);
            return txFunction.apply(wrappedTx);
        }
    }

    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception, R>
        FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txFunction) {
        return applyWithNewTransactionAndSubmit(datastoreType, transactionFactory::newReadWriteTransaction,
            TypedReadWriteTransactionImpl::new, txFunction, (realTx, wrappedTx) -> realTx.commit());
    }

    @Override
    public <D extends Datastore, E extends Exception> void callInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedConsumer<TypedReadTransaction<D>, E> txConsumer)
            throws E, InterruptedException {
        try (ReadTransaction realTx = transactionFactory.newReadOnlyTransaction()) {
            TypedReadTransaction<D> wrappedTx = new TypedReadTransactionImpl<>(datastoreType, realTx);
            txConsumer.accept(wrappedTx);
        }
    }

    @Override
    public <D extends Datastore, E extends Exception> void callWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, CheckedConsumer<TypedReadTransaction<D>, E> txConsumer) throws E {
        try (ReadTransaction realTx = transactionFactory.newReadOnlyTransaction()) {
            TypedReadTransaction<D> wrappedTx = new TypedReadTransactionImpl<>(datastoreType, realTx);
            txConsumer.accept(wrappedTx);
        }
    }

    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception>
        FluentFuture<? extends Object> callWithNewReadWriteTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txConsumer) {
        return callWithNewTransactionAndSubmit(datastoreType, transactionFactory::newReadWriteTransaction,
            TypedReadWriteTransactionImpl::new, txConsumer, (realTx, wrappedTx) -> realTx.commit());
    }

    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception> FluentFuture<? extends Object> callWithNewWriteOnlyTransactionAndSubmit(
            Class<D> datastoreType, InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txConsumer) {
        return callWithNewTransactionAndSubmit(datastoreType, transactionFactory::newWriteOnlyTransaction,
            TypedWriteTransactionImpl::new, txConsumer, (realTx, wrappedTx) -> realTx.commit());
    }

    @CheckReturnValue
    protected <D extends Datastore, T extends WriteTransaction, W, E extends Exception> FluentFuture<? extends Object>
        callWithNewTransactionAndSubmit(
            Class<D> datastoreType, Supplier<T> txSupplier, BiFunction<Class<D>, T, W> txWrapper,
            InterruptibleCheckedConsumer<W, E> txConsumer, BiFunction<T, W, FluentFuture<?>> txSubmitter) {
        return applyWithNewTransactionAndSubmit(datastoreType, txSupplier, txWrapper, tx -> {
            txConsumer.accept(tx);
            return null;
        }, txSubmitter);
    }

    @CheckReturnValue
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected <D extends Datastore, T extends WriteTransaction, W, R, E extends Exception> FluentFuture<R>
        applyWithNewTransactionAndSubmit(
            Class<D> datastoreType, Supplier<T> txSupplier, BiFunction<Class<D>, T, W> txWrapper,
            InterruptibleCheckedFunction<W, R, E> txFunction, BiFunction<T, W, FluentFuture<?>> txSubmitter) {
        T realTx = txSupplier.get();
        W wrappedTx = txWrapper.apply(datastoreType, realTx);
        R result;
        try {
            // We must store the result before submitting the transaction; if we inline the next line in the
            // transform lambda, that's not guaranteed
            result = txFunction.apply(wrappedTx);
        } catch (Exception e) {
            // catch Exception for both the <E extends Exception> thrown by accept() as well as any RuntimeException
            if (!realTx.cancel()) {
                LOG.error("Transaction.cancel() returned false - this should never happen (here)");
            }
            return FluentFutures.immediateFailedFluentFuture(e);
        }
        return txSubmitter.apply(realTx, wrappedTx).transform(v -> result, MoreExecutors.directExecutor());
    }

    protected T getTransactionFactory() {
        return transactionFactory;
    }
}
