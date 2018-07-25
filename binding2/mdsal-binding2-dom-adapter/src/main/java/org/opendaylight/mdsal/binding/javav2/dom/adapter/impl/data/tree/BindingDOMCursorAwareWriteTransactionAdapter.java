/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import com.google.common.annotations.Beta;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.CursorAwareWriteTransaction;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeWriteCursor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Allowing cursor to provides write operations(delete, merge, write).
 *
 * @param <T>
 *            - {@link DOMDataTreeCursorAwareTransaction} type
 */
@Beta
public class BindingDOMCursorAwareWriteTransactionAdapter<T extends DOMDataTreeCursorAwareTransaction>
        implements CursorAwareWriteTransaction {

    private final T delegate;
    private final BindingToNormalizedNodeCodec codec;

    public BindingDOMCursorAwareWriteTransactionAdapter(final T delegate, final BindingToNormalizedNodeCodec codec) {
        this.delegate = delegate;
        this.codec = codec;
    }

    @Nullable
    @Override
    public <P extends TreeNode> DataTreeWriteCursor createCursor(@Nonnull final DataTreeIdentifier<P> path) {
        final YangInstanceIdentifier yPath = codec.toNormalized(path.getRootIdentifier());
        final DOMDataTreeWriteCursor cursor =
                delegate.createCursor(new DOMDataTreeIdentifier(path.getDatastoreType(), yPath));
        return new BindingDOMDataTreeWriteCursorAdapter<>(path, cursor, codec);
    }

    @Override
    public boolean cancel() {
        return delegate.cancel();
    }

    @Override
    public <V extends TreeNode> void submit(final BiConsumer<TransactionCommitFailedException, V> callback) {
        delegate.commit();
    }
}

