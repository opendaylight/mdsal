/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadOperations;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

abstract class AbstractForwardedTransaction<T extends DOMDataTreeTransaction> implements Delegator<T>,
        Identifiable<Object> {

    private final @NonNull BindingToNormalizedNodeCodec codec;
    private final @NonNull T delegate;

    AbstractForwardedTransaction(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        this.delegate = requireNonNull(delegateTx, "Delegate must not be null");
        this.codec = requireNonNull(codec, "Codec must not be null");
    }

    @Override
    public final Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }

    protected final <S extends DOMDataTreeTransaction> S getDelegateChecked(final Class<S> txType) {
        checkState(txType.isInstance(delegate));
        return txType.cast(delegate);
    }

    protected final BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    protected final <D extends DataObject> @NonNull FluentFuture<Optional<D>> doRead(
            final DOMDataTreeReadOperations readOps, final LogicalDatastoreType store,
            final InstanceIdentifier<D> path) {
        checkArgument(!path.isWildcarded(), "Invalid read of wildcarded path %s", path);

        return readOps.read(store, codec.toYangInstanceIdentifierBlocking(path))
                .transform(codec.getCodecRegistry().deserializeFunction(path)::apply, MoreExecutors.directExecutor());
    }

    protected final @NonNull FluentFuture<Boolean> doExists(final DOMDataTreeReadOperations readOps,
            final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        checkArgument(!path.isWildcarded(), "Invalid exists of wildcarded path %s", path);
        return readOps.exists(store, codec.toYangInstanceIdentifierBlocking(path));
    }
}
