/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.util.concurrent.FluentFuture;
import java.util.function.Function;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public final class OSGiManagedNewTransactionRunner implements ManagedNewTransactionRunner {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiManagedNewTransactionRunner.class);

    @Reference
    DataBroker dataBroker = null;

    private @Nullable ManagedNewTransactionRunnerImpl delegate;

    @Override
    public <D extends Datastore, E extends Exception, R> R applyInterruptiblyWithNewReadOnlyTransactionAndClose(
            final Class<D> datastoreType, final InterruptibleCheckedFunction<TypedReadTransaction<D>, R, E> txFunction)
                    throws E, InterruptedException {
        return delegate.applyInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction);
    }

    @Override
    public <D extends Datastore, E extends Exception, R> R applyWithNewReadOnlyTransactionAndClose(
            final Class<D> datastoreType, final CheckedFunction<TypedReadTransaction<D>, R, E> txFunction) throws E {
        return delegate.applyWithNewReadOnlyTransactionAndClose(datastoreType, txFunction);
    }

    @Override
    public <D extends Datastore, E extends Exception, R> FluentFuture<R> applyWithNewReadWriteTransactionAndSubmit(
            final Class<D> datastoreType,
            final InterruptibleCheckedFunction<TypedReadWriteTransaction<D>, R, E> txFunction) {
        return delegate.applyWithNewReadWriteTransactionAndSubmit(datastoreType, txFunction);
    }

    @Override
    public <D extends Datastore, E extends Exception> void callInterruptiblyWithNewReadOnlyTransactionAndClose(
            final Class<D> datastoreType, final InterruptibleCheckedConsumer<TypedReadTransaction<D>, E> txConsumer)
                    throws E, InterruptedException {
        delegate.callInterruptiblyWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer);
    }

    @Override
    public <D extends Datastore, E extends Exception> void callWithNewReadOnlyTransactionAndClose(
            final Class<D> datastoreType, final CheckedConsumer<TypedReadTransaction<D>, E> txConsumer) throws E {
        delegate.callWithNewReadOnlyTransactionAndClose(datastoreType, txConsumer);
    }

    @Override
    public <D extends Datastore, E extends Exception>
            FluentFuture<? extends Object> callWithNewReadWriteTransactionAndSubmit(final Class<D> datastoreType,
                    final InterruptibleCheckedConsumer<TypedReadWriteTransaction<D>, E> txConsumer) {
        return delegate.callWithNewReadWriteTransactionAndSubmit(datastoreType, txConsumer);
    }

    @Override
    public <D extends Datastore, E extends Exception>
            FluentFuture<? extends Object> callWithNewWriteOnlyTransactionAndSubmit(final Class<D> datastoreType,
                    final InterruptibleCheckedConsumer<TypedWriteTransaction<D>, E> txConsumer) {
        return delegate.callWithNewWriteOnlyTransactionAndSubmit(datastoreType, txConsumer);
    }

    @Override
    public <R> R applyWithNewTransactionChainAndClose(final Function<ManagedTransactionChain, R> chainConsumer) {
        return delegate.applyWithNewTransactionChainAndClose(chainConsumer);
    }

    @Activate
    void activate() {
        delegate = new ManagedNewTransactionRunnerImpl(dataBroker);
        LOG.info("Managed Transaction Runner activated");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Managed Transaction Runner deactivated");
    }
}
