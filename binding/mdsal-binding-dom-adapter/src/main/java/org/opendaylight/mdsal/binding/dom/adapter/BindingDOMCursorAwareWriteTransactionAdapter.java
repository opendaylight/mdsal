/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.CursorAwareWriteTransaction;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeWriteCursor;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class BindingDOMCursorAwareWriteTransactionAdapter<T extends DOMDataTreeCursorAwareTransaction> implements CursorAwareWriteTransaction {

    private T delegate;
    private BindingToNormalizedNodeCodec codec;

    public BindingDOMCursorAwareWriteTransactionAdapter(final T delegate, final BindingToNormalizedNodeCodec codec) {

        this.delegate = delegate;
        this.codec = codec;
    }

    @Nullable
    @Override
    public <T extends DataObject> DataTreeWriteCursor createCursor(final DataTreeIdentifier<T> path) {
        final YangInstanceIdentifier yPath = codec.toNormalized(path.getRootIdentifier());
        final DOMDataTreeWriteCursor cursor = delegate.createCursor(new DOMDataTreeIdentifier(path.getDatastoreType(), yPath));
        return new BindingDOMDataTreeWriteCursorAdapter<>(path, cursor, codec);
    }

    @Override
    public boolean cancel() {
        return delegate.cancel();
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> submit() {
        return delegate.submit();
    }

}
