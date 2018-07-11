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

public class BindingDOMCursorAwareWriteTransactionAdapter<T extends DOMDataTreeCursorAwareTransaction>
        extends AbstractBindingAdapter<T> implements CursorAwareWriteTransaction {

    public BindingDOMCursorAwareWriteTransactionAdapter(final T delegate, final BindingToNormalizedNodeCodec codec) {
        super(codec, delegate);
    }

    @Nullable
    @Override
    public <P extends DataObject> DataTreeWriteCursor createCursor(final DataTreeIdentifier<P> path) {
        final YangInstanceIdentifier yPath = getCodec().toNormalized(path.getRootIdentifier());
        final DOMDataTreeWriteCursor cursor = getDelegate().createCursor(
                new DOMDataTreeIdentifier(path.getDatastoreType(), yPath));
        return new BindingDOMDataTreeWriteCursorAdapter<>(path, cursor, getCodec());
    }

    @Override
    public boolean cancel() {
        return getDelegate().cancel();
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> submit() {
        return getDelegate().submit();
    }
}
