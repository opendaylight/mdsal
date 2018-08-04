/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract class for forwards transaction and codec for serialize/deserialize DOM and Binding data.
 *
 * @param <T>
 *            - type of asynchronous transaction
 */
@Beta
public abstract class AbstractForwardedTransaction<
        T extends AsyncTransaction<YangInstanceIdentifier, NormalizedNode<?, ?>>>
        implements Delegator<T>, Identifiable<Object> {

    private final T delegate;
    private final BindingToNormalizedNodeCodec codec;

    public AbstractForwardedTransaction(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        this.delegate = Preconditions.checkNotNull(delegateTx, "Delegate must not be null");
        this.codec = Preconditions.checkNotNull(codec, "Codec must not be null");
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
    protected final <S extends AsyncTransaction<YangInstanceIdentifier, NormalizedNode<?, ?>>> S
            getDelegateChecked(final Class<S> txType) {
        Preconditions.checkState(txType.isInstance(delegate));
        return (S) delegate;
    }

    protected final BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    protected final <D extends TreeNode> FluentFuture<Optional<D>> doRead(
            final DOMDataTreeReadTransaction readTx, final LogicalDatastoreType store,
            final InstanceIdentifier<D> path) {
        Preconditions.checkArgument(!path.isWildcarded(), "Invalid read of wildcarded path %s", path);

        return readTx.read(store, codec.toYangInstanceIdentifierBlocking(path))
            .transform(Optional::fromJavaUtil, MoreExecutors.directExecutor())
            .transform(codec.deserializeFunction(path), MoreExecutors.directExecutor());
    }
}
