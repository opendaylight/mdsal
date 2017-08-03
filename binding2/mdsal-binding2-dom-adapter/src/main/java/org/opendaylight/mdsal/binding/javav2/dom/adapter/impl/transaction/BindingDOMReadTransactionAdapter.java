/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.AbstractForwardedTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;

/**
 * Read transaction adapter.
 */
@Beta
public class BindingDOMReadTransactionAdapter extends AbstractForwardedTransaction<DOMDataTreeReadTransaction>
        implements ReadTransaction {

    public BindingDOMReadTransactionAdapter(final DOMDataTreeReadTransaction delegate,
            final BindingToNormalizedNodeCodec codec) {
        super(delegate, codec);
    }

    @Override
    public <T extends TreeNode> void read(final LogicalDatastoreType store, final InstanceIdentifier<T> path,
            final BiConsumer<ReadFailedException, T> callback) {
        doRead(getDelegate(), store, path);
    }

    @Override
    public <T extends TreeNode> CheckedFuture<Optional<T>, ReadFailedException> read(LogicalDatastoreType store, InstanceIdentifier<T> path) {
        return doRead(getDelegate(), store, path);
    }

    @Override
    public void close() {
        getDelegate().close();
    }
}