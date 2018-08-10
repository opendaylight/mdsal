/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Provides access to registered Remote Procedure Call (RPC) and Action service implementations.
 * RPCs and Actions are defined in YANG models.
 *
 * <p>
 * RPC/Action implementations are registered using the {@link RpcActionProviderService}.
 */
@Beta
public interface RpcActionConsumerRegistry extends BindingService {

    /**
     * Returns an implementation of a requested RPC service.
     *
     * <p>
     * The returned instance is not an actual implementation of the RPC service interface, but a
     * proxy implementation of the interface that forwards to an actual implementation, if any.
     *
     * @param serviceInterface given service interface
     * @param <T> interface type
     * @return returns proxy for the requested RPC
     */
    <T extends Rpc<?, ?>> T getRpcService(Class<T> serviceInterface);

    /**
     * Returns an implementation of a requested Action service.
     *
     * <p>
     * The returned instance is not an actual implementation of the Action service interface, but a
     * proxy implementation of the interface that forwards to an actual implementation, if any.
     *
     * @param serviceInterface given service interface
     * @param <T> interface type
     * @return returns proxy for the requested Action
     */
    <T extends Action<? extends TreeNode, ?, ?, ?>> T getActionService(Class<T> serviceInterface);

    /**
     * Returns an implementation of a requested ListAction service.
     *
     * <p>
     * The returned instance is not an actual implementation of the ListAction service interface, but a
     * proxy implementation of the interface that forwards to an actual implementation, if any.
     *
     * @param serviceInterface given service interface
     * @param <T> interface type
     * @return returns proxy for the requested ListAction
     */
    <T extends ListAction<? extends TreeNode, ?, ?, ?>> T getListActionService(Class<T> serviceInterface);

}
