/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.ReadFailedException;

/**
 * Implementation of {@link ManagedNewTransactionRunner} with automatic transparent retries on transaction failure
 * ({@link OptimisticLockFailedException} on write transactions and {@link ReadFailedException} on read transactions
 * will cause the operation constructing the transaction to be re-run).
 * This is a package local private internal class; end-users use the {@link RetryingManagedNewTransactionRunner}.
 * @see RetryingManagedNewTransactionRunner
 *
 * @author Michael Vorburger.ch &amp; Stephen Kitt, with input from Tom Pantelis re. catchingAsync &amp; direct Executor
 */
// intentionally package local
class RetryingManagedNewTransactionRunnerImpl implements ManagedNewTransactionRunner {

    // NB: The RetryingManagedNewTransactionRunnerTest is in mdsalutil-testutils's src/test, not this project's

    // duplicated in SingleTransactionDataBroker
    private static final int DEFAULT_RETRIES = 3;

    private final int maxRetries;

    private final ManagedNewTransactionRunner delegate;

    private final Executor executor;

    RetryingManagedNewTransactionRunnerImpl(ManagedNewTransactionRunner delegate) {
        this(delegate, MoreExecutors.directExecutor(), DEFAULT_RETRIES);
    }

    RetryingManagedNewTransactionRunnerImpl(ManagedNewTransactionRunner delegate, int maxRetries) {
        this(delegate, MoreExecutors.directExecutor(), maxRetries);
    }

    RetryingManagedNewTransactionRunnerImpl(ManagedNewTransactionRunner delegate, Executor executor, int maxRetries) {
        this.delegate = requireNonNull(delegate, "delegate must not be null");
        this.executor = requireNonNull(executor, "executor must not be null");
        this.maxRetries = maxRetries;
    }

    @Override
    public <D extends Datastore, E extends Exception, R> R applyInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadTransaction<D>, R, E> txFunction)
            throws E, InterruptedException {
        return applyInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction, maxRetries);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private <R, D extends Datastore, E extends Exception> R applyInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadTransaction<D>, R, E> txFunction,
            int tries) throws E, InterruptedException {
        try {
            return delegate.applyInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction);
        } catch (Exception e) {
            if (isRetriableException(e) && tries - 1 > 0) {
                return applyInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction, tries - 1);
            } else {
                throw e;
            }
        }
    }

    @Override
    public <D extends Datastore, E extends Exception, R> R applyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, CheckedFunction<TypedReadTransaction<D>, R, E> txFunction) throws E {
        return applyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction, maxRetries);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private <R, D extends Datastore, E extends Exception> R applyWithNewReadOnlyTransactionAndClose(
        Class<D> datastoreType, CheckedFunction<TypedReadTransaction<D>, R, E> txFunction, int tries) throws E {
        try {
            return delegate.applyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction);
        } catch (Exception e) {
            if (isRetriableException(e) && tries - 1 > 0) {
                return applyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction, tries - 1);
            } else {
                throw e;
            }
        }
    }

    @Override
    public <D extends Datastore, E extends Exception, R> FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(
            Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txFunction) {
        return applyWithNewReadWriteTransactionAndSubmit(datastoreType, txFunction, maxRetries);
    }

    private <D extends Datastore, E extends Exception, R> FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(
            Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txRunner,
            int tries) {
        FluentFuture<R> future = requireNonNull(
            delegate.applyWithNewReadWriteTransactionAndSubmit(datastoreType, txRunner),
            "delegate.callWithNewReadWriteTransactionAndSubmit() == null");
        return future.catchingAsync(Exception.class, exception -> {
            if (isRetriableException(exception) && tries - 1 > 0) {
                return applyWithNewReadWriteTransactionAndSubmit(datastoreType, txRunner, tries - 1);
            } else {
                throw exception;
            }
        }, executor);
    }

    @Override
    public <R> R applyWithNewTransactionChainAndClose(Function<ManagedTransactionChain, R> chainConsumer) {
        throw new UnsupportedOperationException("The retrying transaction manager doesn't support transaction chains");
    }

    @Override
    public <D extends Datastore, E extends Exception> void callInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedConsumer<TypedReadTransaction<D>, E> txConsumer)
            throws E, InterruptedException {
        callInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer, maxRetries);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private <D extends Datastore, E extends Exception> void callInterruptiblyWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, InterruptibleCheckedConsumer<TypedReadTransaction<D>, E> txConsumer, int tries)
            throws E, InterruptedException {
        try {
            delegate.callInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer);
        } catch (Exception e) {
            if (isRetriableException(e) && tries - 1 > 0) {
                callInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer, tries - 1);
            } else {
                throw e;
            }
        }
    }

    @Override
    public <D extends Datastore, E extends Exception> void callWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, CheckedConsumer<TypedReadTransaction<D>, E> txConsumer) throws E {
        callWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer, maxRetries);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private <D extends Datastore, E extends Exception> void callWithNewReadOnlyTransactionAndClose(
            Class<D> datastoreType, CheckedConsumer<TypedReadTransaction<D>, E> txConsumer, int tries) throws E {
        try {
            delegate.callWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer);
        } catch (Exception e) {
            if (isRetriableException(e) && tries - 1 > 0) {
                callWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer, tries - 1);
            } else {
                throw e;
            }
        }
    }

    @Override
    public <D extends Datastore, E extends Exception> FluentFuture<? extends Object>
        callWithNewReadWriteTransactionAndSubmit(
            Class<D> datastoreType, InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txConsumer) {
        return callWithNewReadWriteTransactionAndSubmit(datastoreType, txConsumer, maxRetries);
    }

    private <D extends Datastore, E extends Exception, T> FluentFuture<T>
        callWithNewReadWriteTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txRunner, int tries) {

        return (FluentFuture<T>) requireNonNull(
            delegate.callWithNewReadWriteTransactionAndSubmit(datastoreType, txRunner),
            "delegate.callWithNewWriteOnlyTransactionAndSubmit() == null")
            .catchingAsync(Exception.class, exception -> {
                // as per AsyncWriteTransaction.submit()'s JavaDoc re. retries
                if (isRetriableException(exception) && tries - 1 > 0) {
                    return callWithNewReadWriteTransactionAndSubmit(datastoreType, txRunner, tries - 1);
                } else {
                    // out of retries, so propagate the exception
                    throw exception;
                }
            }, executor);
    }

    @Override
    public <D extends Datastore, E extends Exception> FluentFuture<? extends Object>
        callWithNewWriteOnlyTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txConsumer) {
        return callWithNewWriteOnlyTransactionAndSubmit(datastoreType, txConsumer, maxRetries);
    }

    private <D extends Datastore, E extends Exception, T> FluentFuture<T>
        callWithNewWriteOnlyTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txRunner, int tries) {

        return (FluentFuture<T>) requireNonNull(
            delegate.callWithNewWriteOnlyTransactionAndSubmit(datastoreType, txRunner),
            "delegate.callWithNewWriteOnlyTransactionAndSubmit() == null")
            .catchingAsync(OptimisticLockFailedException.class, optimisticLockFailedException -> {
                // as per AsyncWriteTransaction.submit()'s JavaDoc re. retries
                if (tries - 1 > 0) {
                    return callWithNewWriteOnlyTransactionAndSubmit(datastoreType, txRunner, tries - 1);
                } else {
                    // out of retries, so propagate the OptimisticLockFailedException
                    throw optimisticLockFailedException;
                }
            }, executor);
    }

    private boolean isRetriableException(Throwable throwable) {
        return throwable instanceof OptimisticLockFailedException || throwable instanceof ReadFailedException || (
            throwable instanceof ExecutionException && isRetriableException(throwable.getCause()));
    }
}
