/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import com.google.common.util.concurrent.FluentFuture;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.spi.ForwardingWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;

/**
 * WriteTransaction which cannot be {@link WriteTransaction#cancel()} or
 * {@link WriteTransaction#commit()}.
 *
 * @author Michael Vorburger.ch
 */
@SuppressWarnings("deprecation")
// intentionally package local, for now
class NonSubmitCancelableWriteTransaction extends ForwardingWriteTransaction {

    NonSubmitCancelableWriteTransaction(WriteTransaction delegate) {
        super(delegate);
    }

    @Override
    public boolean cancel() {
        throw new UnsupportedOperationException("cancel() cannot be used inside a Managed[New]TransactionRunner");
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        throw new UnsupportedOperationException("commit() cannot be used inside a Managed[New]TransactionRunner");
    }
}
