/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Abstract class for forwards transaction and codec for serialize/deserialize DOM and Binding data.
 *
 * @param <T> type of asynchronous transaction
 */
@Beta
public abstract class AbstractForwardedTransaction<T extends DOMDataTreeTransaction>
        implements Delegator<T>, Identifiable<Object> {

    private final T delegate;
    private final BindingToNormalizedNodeCodec codec;

    public AbstractForwardedTransaction(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        this.delegate = requireNonNull(delegateTx, "Delegate must not be null");
        this.codec = requireNonNull(codec, "Codec must not be null");
    }

    @Nonnull
    @Override
    public final Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    protected final <S extends DOMDataTreeTransaction> S getDelegateChecked(final Class<S> txType) {
        checkState(txType.isInstance(delegate));
        return (S) delegate;
    }

    protected final BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    protected final <D extends TreeNode> FluentFuture<Optional<D>> doRead(
            final DOMDataTreeReadTransaction readTx, final LogicalDatastoreType store,
            final InstanceIdentifier<D> path) {
        checkArgument(!path.isWildcarded(), "Invalid read of wildcarded path %s", path);

        return readTx.read(store, codec.toYangInstanceIdentifierBlocking(path))
            .transform(codec.deserializeFunction(path)::apply, MoreExecutors.directExecutor());
    }
}
