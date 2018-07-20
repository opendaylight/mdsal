/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.util;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.common.api.AsyncReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.Path;

/**
 * Utility {@link AsyncReadWriteTransaction} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public class ForwardingAsyncReadWriteTransaction<P extends Path<P>, D> extends ForwardingObject
        implements AsyncReadWriteTransaction<P, D> {

    private final AsyncReadWriteTransaction<P, D> delegate;

    protected ForwardingAsyncReadWriteTransaction(AsyncReadWriteTransaction<P, D> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected AsyncReadWriteTransaction<P, D> delegate() {
        return delegate;
    }

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public boolean cancel() {
        return delegate.cancel();
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        return delegate.commit();
    }

    @Override
    public void delete(LogicalDatastoreType store, P path) {
        delegate.delete(store, path);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
