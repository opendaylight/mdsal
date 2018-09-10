/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.annotations.Beta;
import java.util.function.Function;
import org.opendaylight.mdsal.binding.api.DataBroker;

/**
 * Managed transactions utility to simplify handling of new transactions and ensure they are always closed.
 * Implementation in {@link ManagedNewTransactionRunnerImpl}, alternative implementation of this API with optional
 * retries is {@link RetryingManagedNewTransactionRunner}.
 *
 * <p>This should typically be used (only) directly in code which really must be creating its own new transactions,
 * such as RPC entry points, or background jobs.  Other lower level code "behind" such entry points should
 * just get handed over the transaction provided by this API.
 */
@Beta
public interface ManagedNewTransactionRunner extends ManagedTransactionFactory {

    /**
     * Invokes a function with a new {@link ManagedTransactionChain}, which is a wrapper around standard transaction
     * chains providing managed semantics. The transaction chain will be closed when the function returns.
     *
     * <p>This is an asynchronous API, like {@link DataBroker}'s own; when this method returns, the transactions in
     * the chain may well still be ongoing in the background, or pending. <strong>It is up to the consumer and
     * caller</strong> to agree on how failure will be handled; for example, the return type can include the futures
     * corresponding to the transactions in the chain. The implementation uses a default transaction chain listener
     * which logs an error if any of the transactions fail.
     *
     * <p>The MD-SAL transaction chain semantics are preserved: each transaction in the chain will see the results of
     * the previous transactions in the chain, even if they haven't been fully committed yet; and any error will result
     * in subsequent transactions in the chain <strong>not</strong> being submitted.
     *
     * @param chainConsumer The {@link Function} that will build transactions in the transaction chain.
     * @param <R> The type of result returned by the function.
     * @return The result of the function call.
     */
    <R> R applyWithNewTransactionChainAndClose(Function<ManagedTransactionChain, R> chainConsumer);

}
