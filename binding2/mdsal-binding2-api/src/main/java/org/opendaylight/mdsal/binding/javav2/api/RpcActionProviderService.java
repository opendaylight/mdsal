/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.KeyedInstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Provides ability to register Remote Procedure Call (RPC) and Action service implementations.
 * The RPCs and Actions are defined in YANG models.
 */
@Beta
public interface RpcActionProviderService {

    /**
     * Returns class representing registration of global RPC.
     * @param type RPC binding generated interface
     * @param implementation RPC binding implementation
     * @param <S> service class type
     * @param <T> service implementation type
     * @return returns class representing a RPC registration
     */
    <S extends Rpc, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
        T implementation);

    /**
     * Returns class representing registration of global RPC for supported paths.
     * @param type RPC binding generated interface
     * @param implementation RPC binding implementation
     * @param paths set of supported paths
     * @param <S> service class type
     * @param <T> service implementation type
     * @return returns class representing a RPC registration
     */
    <S extends Rpc, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
        T implementation, Set<InstanceIdentifier<?>> paths);

    /**
     * Returns class representing registration of Action.
     * @param type Action binding generated interface
     * @param parent parent node for Action connected to
     * @param implementation Action binding implementation
     * @param <S> service class type
     * @param <P> parent type
     * @param <T> service implementation type
     * @return returns class representing a Action registration
     */
    <S extends Action, T extends S, P extends TreeNode> ObjectRegistration<T> registerActionImplementation(
        Class<S> type, InstanceIdentifier<P> parent, T implementation);

    /**
     * Returns class representing registration of ListAction.
     * @param type ListAction binding generated interface
     * @param parent parent node for ListAction connected to
     * @param implementation ListAction binding implementation
     * @param <S> service class type
     * @param <P> parent type
     * @param <K> key type
     * @param <T> service implementation type
     * @return returns class representing a ListAction registration
     */
    <S extends ListAction, T extends S, P extends TreeNode, K> ObjectRegistration<T> registerListActionImplementation(
            Class<S> type, KeyedInstanceIdentifier<P, K> parent, T implementation);
}
