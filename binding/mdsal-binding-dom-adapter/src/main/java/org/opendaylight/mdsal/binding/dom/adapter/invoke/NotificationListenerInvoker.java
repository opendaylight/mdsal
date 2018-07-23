/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;

public final class NotificationListenerInvoker {
    private final org.opendaylight.yangtools.yang.binding.util.NotificationListenerInvoker delegate;

    private NotificationListenerInvoker(
            final org.opendaylight.yangtools.yang.binding.util.NotificationListenerInvoker delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Creates RPCServiceInvoker for specified RpcService type.
     *
     * @param type
     *            RpcService interface, which was generated from model.
     * @return Cached instance of {@link NotificationListenerInvoker} for
     *         supplied RPC type.
     */
    public static NotificationListenerInvoker from(final Class<? extends NotificationListener> type) {
        return new NotificationListenerInvoker(
            org.opendaylight.yangtools.yang.binding.util.NotificationListenerInvoker.from(type));
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl
     *            Imlementation on which notifiaction callback should be
     *            invoked.
     * @param rpcName
     *            Name of RPC to be invoked.
     * @param input
     *            Input data for RPC.
     */
    @SuppressWarnings("checkstyle:illegalCatch")
    public void invokeNotification(@Nonnull final NotificationListener impl, @Nonnull final QName rpcName,
            @Nullable final DataContainer input) {
        delegate.invokeNotification(impl, rpcName, input);
    }
}
