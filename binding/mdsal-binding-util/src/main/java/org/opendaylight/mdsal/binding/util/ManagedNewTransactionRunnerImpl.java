/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Function;
import javax.inject.Inject;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ManagedNewTransactionRunner}. This is based on {@link ManagedTransactionFactoryImpl} but
 * re-implements operations based on read-write transactions to cancel transactions which don't end up making any
 * changes to the datastore.
 */
@Beta
// Do *NOT* mark this as @Singleton, because users choose their implementation
public class ManagedNewTransactionRunnerImpl extends ManagedTransactionFactoryImpl<DataBroker>
        implements ManagedNewTransactionRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ManagedNewTransactionRunnerImpl.class);

    @Inject
    public ManagedNewTransactionRunnerImpl(final DataBroker broker) {
        // Early check to ensure the error message is understandable for the caller
        super(requireNonNull(broker, "broker must not be null"));
    }

    // This is overridden to use this class’s commit method
    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception, R> FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(
            final D datastore, final InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txFunction) {
        return super.applyWithNewTransactionAndSubmit(datastore, getTransactionFactory()::newReadWriteTransaction,
            WriteTrackingTypedReadWriteTransactionImpl::new, txFunction::apply, this::commit);
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public <R> R applyWithNewTransactionChainAndClose(final Function<ManagedTransactionChain, R> chainConsumer) {
        try (var realTxChain = getTransactionFactory().createTransactionChain()) {
            realTxChain.addCallback(new FutureCallback<>() {
                @Override
                public void onSuccess(final @Nullable Empty result) {
                    // Nothing to do
                }

                @Override
                public void onFailure(final @Nullable Throwable cause) {
                    LOG.error("Error handling a transaction chain", cause);
                }
            });
            return chainConsumer.apply(new ManagedTransactionChainImpl(realTxChain));
        }
    }

    // This is overridden to use this class’s commit method
    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception>
        FluentFuture<? extends Object> callWithNewReadWriteTransactionAndSubmit(final D datastore,
            final InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txConsumer) {
        return callWithNewTransactionAndSubmit(datastore, getTransactionFactory()::newReadWriteTransaction,
            WriteTrackingTypedReadWriteTransactionImpl::new, txConsumer::accept, this::commit);
    }

    // This is overridden to use this class’s commit method
    @Override
    @CheckReturnValue
    public <D extends Datastore, E extends Exception>
        FluentFuture<? extends Object> callWithNewWriteOnlyTransactionAndSubmit(final D datastore,
            final InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txConsumer) {
        return super.callWithNewTransactionAndSubmit(datastore, getTransactionFactory()::newWriteOnlyTransaction,
            WriteTrackingTypedWriteTransactionImpl::new, txConsumer::accept, this::commit);
    }

    @CheckReturnValue
    private FluentFuture<? extends CommitInfo> commit(final WriteTransaction realTx,
            final WriteTrackingTransaction wrappedTx) {
        if (wrappedTx.isWritten()) {
            // The transaction contains changes, commit it
            return realTx.commit();
        }

        // The transaction only handled reads, cancel it
        realTx.cancel();
        return CommitInfo.emptyFluentFuture();
    }
}
