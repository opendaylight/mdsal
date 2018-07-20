/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.spi;

import com.google.common.collect.ForwardingObject;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;

/**
 * Utility {@link ReadTransaction} implementation which forwards all interface method
 * invocation to a delegate instance.
 */
public class ForwardingReadTransaction extends ForwardingObject implements ReadTransaction {

    private final ReadTransaction delegate;

    protected ForwardingReadTransaction(ReadTransaction delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ReadTransaction delegate() {
        return delegate;
    }

    @Override
    public <T extends TreeNode> void read(LogicalDatastoreType store,
            org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier<T> path,
            BiConsumer<ReadFailedException, T> callback) {
        delegate.read(store, path, callback);
    }

    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
