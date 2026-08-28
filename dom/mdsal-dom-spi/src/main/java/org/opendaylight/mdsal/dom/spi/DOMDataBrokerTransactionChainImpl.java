/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NormalizedNode implementation of {@link org.opendaylight.mdsal.common.api.TransactionChain} which is backed
 * by several {@link DOMStoreTransactionChain} differentiated by provided
 * {@link org.opendaylight.mdsal.common.api.LogicalDatastoreType} type.
 *
 */
final class DOMDataBrokerTransactionChainImpl extends AbstractDOMForwardedTransactionFactory<DOMStoreTransactionChain>
        implements DOMTransactionChain {
    private enum State {
        RUNNING,
        CLOSING,
        CLOSED,
        FAILED,
    }

    private static final Logger LOG = LoggerFactory.getLogger(DOMDataBrokerTransactionChainImpl.class);
    private static final VarHandle COUNTER_VH;
    private static final VarHandle STATE_VH;
    private static final VarHandle TXNUM_VH;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            COUNTER_VH = lookup.findVarHandle(DOMDataBrokerTransactionChainImpl.class, "counter", int.class);
            STATE_VH = lookup.findVarHandle(DOMDataBrokerTransactionChainImpl.class, "state", State.class);
            TXNUM_VH = lookup.findVarHandle(DOMDataBrokerTransactionChainImpl.class, "txNum", long.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull SettableFuture<Empty> future = SettableFuture.create();
    private final AbstractDOMDataBroker broker;
    private final long chainId;

    private volatile State state = State.RUNNING;
    // number of transactions which are in the process of being committed
    private volatile int counter = 0;
    // linear counter of transactions
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile long txNum = 0;

    DOMDataBrokerTransactionChainImpl(final long chainId,
            final Map<LogicalDatastoreType, DOMStoreTransactionChain> chains, final AbstractDOMDataBroker broker) {
        super(chains);
        this.chainId = chainId;
        this.broker = requireNonNull(broker);
    }

    private void checkNotFailed() {
        Preconditions.checkState(state != State.FAILED, "Transaction chain has failed");
    }

    @Override
    public ListenableFuture<Empty> future() {
        return future;
    }

    @Override
    protected Object newTransactionIdentifier() {
        return "DOM-CHAIN-" + chainId + "-" + (long) TXNUM_VH.getAndAdd(this, 1L);
    }

    @Override
    protected FluentFuture<? extends CommitInfo> commit(final DOMDataTreeWriteTransaction transaction,
            final DOMStoreThreePhaseCommitCohort cohort) {
        checkNotFailed();
        checkNotClosed();

        final var ret = broker.commit(transaction, cohort);
        COUNTER_VH.getAndAdd(this, 1);

        ret.addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                transactionCompleted();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                transactionFailed(transaction, throwable);
            }
        }, MoreExecutors.directExecutor());

        return ret;
    }

    @Override
    public void close() {
        if (!STATE_VH.compareAndSet(this, State.RUNNING, State.CLOSING)) {
            LOG.debug("Chain {} is no longer running", this);
            return;
        }

        super.close();
        for (var subChain : getTxFactories().values()) {
            subChain.close();
        }

        if (counter == 0) {
            finishClose();
        }
    }

    private void finishClose() {
        state = State.CLOSED;
        future.set(Empty.value());
    }

    private void transactionCompleted() {
        if ((int) COUNTER_VH.getAndAdd(this, -1) == 1 && state == State.CLOSING) {
            finishClose();
        }
    }

    private void transactionFailed(final DOMDataTreeWriteTransaction tx, final Throwable cause) {
        state = State.FAILED;
        LOG.debug("Transaction chain {} failed.", this, cause);
        future.setException(cause);
    }
}
