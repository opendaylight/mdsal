/*
 * Copyright © 2017, 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

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
    public <T extends DataObject> FluentFuture<Optional<T>> read(LogicalDatastoreType store,
            InstanceIdentifier<T> path) {
        return delegate.read(store, path);
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
