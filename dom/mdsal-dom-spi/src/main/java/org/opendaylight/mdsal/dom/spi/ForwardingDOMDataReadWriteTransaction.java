/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Utility {@link DOMDataTreeReadWriteTransaction} implementation which forwards all interface
 * method invocation to a delegate instance.
 */
public abstract class ForwardingDOMDataReadWriteTransaction extends ForwardingObject
        implements DOMDataTreeReadWriteTransaction {
    @Override
    protected abstract @NonNull DOMDataTreeReadWriteTransaction delegate();

    @Override
    public FluentFuture<Optional<NormalizedNode>> read(final YangInstanceIdentifier path) {
        return delegate().read(path);
    }

    @Override
    public FluentFuture<Boolean> exists(final YangInstanceIdentifier path) {
        return delegate().exists(path);
    }

    @Override
    public Object getIdentifier() {
        return delegate().getIdentifier();
    }

    @Override
    public void put(final YangInstanceIdentifier path, final NormalizedNode data) {
        delegate().put(path, data);
    }

    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode data) {
        delegate().merge(path, data);
    }

    @Override
    public boolean cancel() {
        return delegate().cancel();
    }

    @Override
    public void delete(final YangInstanceIdentifier path) {
        delegate().delete(path);
    }

    @Override
    public void commit(final CommitCallback callback) {
        delegate().commit(callback);
    }

    @Override
    public FluentFuture<?> completionFuture() {
        return delegate().completionFuture();
    }
}
