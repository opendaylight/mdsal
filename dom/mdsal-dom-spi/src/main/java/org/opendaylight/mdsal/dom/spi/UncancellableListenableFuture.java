/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ForwardingListenableFuture.SimpleForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class UncancellableListenableFuture<V> extends SimpleForwardingListenableFuture<V> {
    private static final Logger LOG = LoggerFactory.getLogger(UncancellableListenableFuture.class);

    public UncancellableListenableFuture(final ListenableFuture<V> delegate) {
        super(delegate);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        LOG.debug("Attempted to cancel future", new Throwable());
        return false;
    }
}
