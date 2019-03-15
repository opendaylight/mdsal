/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;

/**
 * Managed transaction factories provide managed transactions, <em>i.e.</em> transactions which are automatically
 * submitted or cancelled (write) or closed (read).
 *
 * <p>
 * This is a common interface for broker- and chain-based transaction managers, and should not be used directly.
 */
@Beta
public interface ManagedTransactionFactory {
    /**
     * Invokes a function with a <b>NEW</b> {@link TypedReadTransaction}, and ensures that that transaction is closed.
     * Thus when this method returns, that transaction is guaranteed to have been closed, and will never "leak" and
     * waste memory.
     *
     * <p>The function must not itself attempt to close the transaction. (It can't directly, since
     * {@link TypedReadTransaction} doesn't expose a {@code close()} method.)
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txFunction the {@link InterruptibleCheckedFunction} that needs a new read transaction
     * @return the result of the function.
     * @throws E if an error occurs.
     * @throws InterruptedException if the function is interrupted (this is passed through from the provided function).
     */
    <D extends Datastore, E extends Exception, R> R applyInterruptiblyWithNewReadOnlyTransactionAndClose(
        Class<D> datastoreType, InterruptibleCheckedFunction<TypedReadTransaction<D>, R, E> txFunction)
        throws E, InterruptedException;

    /**
     * Invokes a function with a <b>NEW</b> {@link TypedReadTransaction}, and ensures that that transaction is closed.
     * Thus when this method returns, that transaction is guaranteed to have been closed, and will never "leak" and
     * waste memory.
     *
     * <p>The function must not itself attempt to close the transaction. (It can't directly, since
     * {@link TypedReadTransaction} doesn't expose a {@code close()} method.)
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txFunction the {@link InterruptibleCheckedFunction} that needs a new read transaction
     * @return the result of the function.
     * @throws E if an error occurs.
     */
    <D extends Datastore, E extends Exception, R> R applyWithNewReadOnlyTransactionAndClose(Class<D> datastoreType,
        CheckedFunction<TypedReadTransaction<D>, R, E> txFunction) throws E;

    /**
     * Invokes a function with a <b>NEW</b> {@link ReadWriteTransaction}, and then submits that transaction and
     * returns the Future from that submission, or cancels it if an exception was thrown and returns a failed
     * future with that exception. Thus when this method returns, that transaction is guaranteed to have
     * been either submitted or cancelled, and will never "leak" and waste memory.
     *
     * <p>The function must not itself use
     * {@link ReadWriteTransaction#cancel()}, or
     * {@link ReadWriteTransaction#commit()} (it will throw an {@link UnsupportedOperationException}).
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * <p>This is an asynchronous API, like {@link DataBroker}'s own;
     * when returning from this method, the operation of the Transaction may well still be ongoing in the background,
     * or pending;
     * calling code therefore <b>must</b> handle the returned future, e.g. by passing it onwards (return),
     * or by itself adding callback listeners to it using {@link Futures}' methods, or by transforming it into a
     * {@link CompletionStage}
     * (but better NOT by using the blocking {@link Future#get()} on it).
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txFunction the {@link InterruptibleCheckedFunction} that needs a new read-write transaction
     * @return the {@link FluentFuture} returned by {@link ReadWriteTransaction#commit()}, or a failed future with an
     *         application specific exception (not from submit())
     */
    @CheckReturnValue
    <D extends Datastore, E extends Exception, R>
        FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txFunction);

    /**
     * Invokes a function with a <b>NEW</b> {@link ReadTransaction}, and ensures that that transaction is closed.
     * Thus when this method returns, that transaction is guaranteed to have been closed, and will never "leak" and
     * waste memory.
     *
     * <p>The function must not itself attempt to close the transaction. (It can't directly, since
     * {@link ReadTransaction} doesn't expose a {@code close()} method.)
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txConsumer the {@link InterruptibleCheckedFunction} that needs a new read transaction
     * @throws E if an error occurs.
     * @throws InterruptedException if the function is interrupted (this is passed through from the provided function).
     */
    <D extends Datastore, E extends Exception> void callInterruptiblyWithNewReadOnlyTransactionAndClose(
        Class<D> datastoreType, InterruptibleCheckedConsumer<TypedReadTransaction<D>, E> txConsumer)
        throws E, InterruptedException;

    /**
     * Invokes a function with a <b>NEW</b> {@link ReadTransaction}, and ensures that that transaction is closed.
     * Thus when this method returns, that transaction is guaranteed to have been closed, and will never "leak" and
     * waste memory.
     *
     * <p>The function must not itself attempt to close the transaction. (It can't directly, since
     * {@link ReadTransaction} doesn't expose a {@code close()} method.)
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txConsumer the {@link InterruptibleCheckedFunction} that needs a new read transaction
     * @throws E if an error occurs.
     */
    <D extends Datastore, E extends Exception> void callWithNewReadOnlyTransactionAndClose(Class<D> datastoreType,
        CheckedConsumer<TypedReadTransaction<D>, E> txConsumer) throws E;

    /**
     * Invokes a consumer with a <b>NEW</b> {@link ReadWriteTransaction}, and then submits that transaction and
     * returns the Future from that submission, or cancels it if an exception was thrown and returns a failed
     * future with that exception. Thus when this method returns, that transaction is guaranteed to have
     * been either submitted or cancelled, and will never "leak" and waste memory.
     *
     * <p>The consumer should not (cannot) itself use
     * {@link ReadWriteTransaction#cancel()}, or
     * {@link ReadWriteTransaction#commit()} (it will throw an {@link UnsupportedOperationException}).
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * <p>This is an asynchronous API, like {@link DataBroker}'s own;
     * when returning from this method, the operation of the Transaction may well still be ongoing in the background,
     * or pending;
     * calling code therefore <b>must</b> handle the returned future, e.g. by passing it onwards (return),
     * or by itself adding callback listeners to it using {@link Futures}' methods, or by transforming it into a
     * {@link CompletionStage}
     * (but better NOT by using the blocking {@link Future#get()} on it).
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txConsumer the {@link InterruptibleCheckedConsumer} that needs a new read-write transaction
     * @return the {@link FluentFuture} returned by {@link ReadWriteTransaction#commit()}, or a failed future with an
     *         application specific exception (not from submit())
     */
    @CheckReturnValue
    <D extends Datastore, E extends Exception>
        FluentFuture<? extends Object> callWithNewReadWriteTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txConsumer);

    /**
     * Invokes a consumer with a <b>NEW</b> {@link WriteTransaction}, and then submits that transaction and
     * returns the Future from that submission, or cancels it if an exception was thrown and returns a failed
     * future with that exception. Thus when this method returns, that transaction is guaranteed to have
     * been either submitted or cancelled, and will never "leak" and waste memory.
     *
     * <p>The consumer should not (cannot) itself use
     * {@link WriteTransaction#cancel()}, or
     * {@link WriteTransaction#commit()} (it will throw an {@link UnsupportedOperationException}).
     *
     * <p>The provided transaction is specific to the given logical datastore type and cannot be used for any
     * other.
     *
     * <p>This is an asynchronous API, like {@link DataBroker}'s own;
     * when returning from this method, the operation of the Transaction may well still be ongoing in the background,
     * or pending;
     * calling code therefore <b>must</b> handle the returned future, e.g. by passing it onwards (return),
     * or by itself adding callback listeners to it using {@link Futures}' methods, or by transforming it into a
     * {@link CompletionStage}
     * (but better NOT by using the blocking {@link Future#get()} on it).
     *
     * @param datastoreType the {@link Datastore} type that will be accessed
     * @param txConsumer the {@link InterruptibleCheckedConsumer} that needs a new write only transaction
     * @return the {@link FluentFuture} returned by {@link WriteTransaction#commit()}, or a failed future with an
     *         application specific exception (not from submit())
     */
    @CheckReturnValue
    <D extends Datastore, E extends Exception>
        FluentFuture<? extends Object> callWithNewWriteOnlyTransactionAndSubmit(Class<D> datastoreType,
            InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txConsumer);
}
