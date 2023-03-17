/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Utility {@link ReadTransaction} implementation which forwards all interface method invocation to a delegate instance.
 */
public class ForwardingReadTransaction extends ForwardingTransaction implements ReadTransaction {
    private final ReadTransaction delegate;

    public ForwardingReadTransaction(final ReadTransaction delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected ReadTransaction delegate() {
        return delegate;
    }

    @Override
    public <T extends DataObject> FluentFuture<Optional<T>> read(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path) {
        return delegate.read(store, path);
    }

    @Override
    public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(final @NonNull LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<T> path) {
        return delegate.read(store, path);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        return delegate.exists(store, path);
    }

    @Override
    public @NonNull FluentFuture<Boolean> exists(final @NonNull LogicalDatastoreType store,
            final org.opendaylight.mdsal.binding.api.@NonNull InstanceIdentifier<?> path) {
        return delegate.exists(store,path);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
