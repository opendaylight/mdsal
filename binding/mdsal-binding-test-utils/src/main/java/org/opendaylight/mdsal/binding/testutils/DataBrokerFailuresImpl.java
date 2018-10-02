/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.spi.ForwardingDataBroker;
import org.opendaylight.mdsal.binding.spi.ForwardingReadWriteTransaction;
import org.opendaylight.mdsal.binding.spi.ForwardingWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataBroker with methods to simulate failures, useful for tests.
 *
 * @author Michael Vorburger.ch
 */
public class DataBrokerFailuresImpl extends ForwardingDataBroker implements DataBrokerFailures {

    private static final Logger LOG = LoggerFactory.getLogger(DataBrokerFailuresImpl.class);

    private final DataBroker delegate;
    private volatile @Nullable ReadFailedException readException;
    private volatile @Nullable TransactionCommitFailedException submitException;
    private final AtomicInteger howManyFailingReads = new AtomicInteger();
    private final AtomicInteger howManyFailingSubmits = new AtomicInteger();
    private boolean submitAndThrowException = false;

    public DataBrokerFailuresImpl(DataBroker delegate) {
        this.delegate = delegate;
    }

    @Override
    protected DataBroker delegate() {
        return delegate;
    }

    @Override
    public void failReads(ReadFailedException exception) {
        unfailReads();
        readException = Objects.requireNonNull(exception, "exception == null");
    }

    @Override
    public void failReads(int howManyTimes, ReadFailedException exception) {
        unfailReads();
        howManyFailingReads.set(howManyTimes);
        readException = Objects.requireNonNull(exception, "exception == null");
    }

    @Override
    public void failSubmits(TransactionCommitFailedException exception) {
        unfailSubmits();
        this.submitException = Objects.requireNonNull(exception, "exception == null");
    }

    @Override
    public void failSubmits(int howManyTimes, TransactionCommitFailedException exception) {
        howManyFailingSubmits.set(howManyTimes);
        this.submitException = Objects.requireNonNull(exception, "exception == null");
    }

    @Override
    public void unfailReads() {
        readException = null;
        howManyFailingReads.set(-1);
    }

    @Override
    public void unfailSubmits() {
        this.submitException = null;
        howManyFailingSubmits.set(-1);
        this.submitAndThrowException = false;
    }

    @Override
    public void failButSubmitsAnyways() {
        unfailSubmits();
        this.submitException = new TransactionCommitFailedException("caused by simulated AskTimeoutException");
        this.submitAndThrowException = true;
    }

    private FluentFuture<? extends CommitInfo> handleCommit(Supplier<FluentFuture<? extends CommitInfo>> commitMethod) {
        if (howManyFailingSubmits.decrementAndGet() == -1) {
            submitException = null;
        }
        if (submitException == null) {
            return commitMethod.get();
        } else {
            if (submitAndThrowException) {
                try {
                    commitMethod.get().get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("Exception while waiting for submitted transaction", e);
                }
            }
            return FluentFuture.from(Futures.immediateFailedFuture(submitException));
        }
    }

    public <T extends DataObject> FluentFuture<Optional<T>> handleRead(
            BiFunction<LogicalDatastoreType, InstanceIdentifier<T>, FluentFuture<Optional<T>>> readMethod,
            LogicalDatastoreType store, InstanceIdentifier<T> path) {
        if (howManyFailingReads.decrementAndGet() == -1) {
            readException = null;
        }
        if (readException == null) {
            return readMethod.apply(store, path);
        } else {
            return FluentFuture.from(Futures.immediateFailedFuture(readException));
        }
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new ForwardingReadWriteTransaction(delegate.newReadWriteTransaction()) {
            @Override
            public <T extends DataObject> FluentFuture<Optional<T>> read(LogicalDatastoreType store,
                    InstanceIdentifier<T> path) {
                return handleRead(super::read, store, path);
            }

            @Override
            public FluentFuture<? extends CommitInfo> commit() {
                return handleCommit(super::commit);
            }
        };
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new ForwardingWriteTransaction(delegate.newWriteOnlyTransaction()) {
            @Override
            public FluentFuture<? extends CommitInfo> commit() {
                return handleCommit(super::commit);
            }
        };
    }

}
