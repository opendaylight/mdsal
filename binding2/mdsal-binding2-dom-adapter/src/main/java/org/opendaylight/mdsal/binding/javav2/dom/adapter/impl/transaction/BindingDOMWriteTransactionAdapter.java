/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.AbstractWriteTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

/**
 * Write transaction adapter.
 *
 * @param <T>
 *            - {@link DOMDataTreeWriteTransaction} type
 */
@Beta
public class BindingDOMWriteTransactionAdapter<T extends DOMDataTreeWriteTransaction>
        extends AbstractWriteTransaction<T>
        implements WriteTransaction {

    public BindingDOMWriteTransactionAdapter(final T delegateTx, final BindingToNormalizedNodeCodec codec) {
        super(delegateTx, codec);
    }

    @Override
    public <U extends TreeNode> void put(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        put(store, path, data, false);
    }

    @Override
    public <U extends TreeNode> void merge(final LogicalDatastoreType store, final InstanceIdentifier<U> path,
            final U data) {
        merge(store, path, data, false);
    }

    @Override
    public void delete(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        doDelete(store, path);
    }

    @Override
    public @NonNull FluentFuture<? extends @NonNull CommitInfo> commit() {
        return doCommit();
    }

    @Override
    public boolean cancel() {
        return doCancel();
    }
}
