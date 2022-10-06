/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.collect.ClassToInstanceMap;
import java.util.Set;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;

/**
 * Provides ability to registered Remote Procedure Call (RPC) service implementations. The RPCs are defined in YANG
 * models.
 */
public interface RpcProviderService extends BindingService {
    /**
     * Register RPCs combined in a {@link RpcService} interface.
     *
     * @param <R> {@link RpcService} type
     * @param <I> {@link RpcService} implementation type
     * @param type {@link RpcService} class
     * @param implementation implementation object
     * @return An {@link ObjectRegistration} controlling unregistration
     * @throws NullPointerException if any argument is {@code null}
     */
    <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(Class<R> type,
        I implementation);

    /**
     * Register RPCs combined in a {@link RpcService} interface on a set of datastore context paths.
     *
     * @param <R> {@link RpcService} type
     * @param <I> {@link RpcService} implementation type
     * @param type {@link RpcService} class
     * @param implementation implementation object
     * @param paths Datastore paths to service
     * @return An {@link ObjectRegistration} controlling unregistration
     * @throws NullPointerException if any argument is {@code null}
     */
    <R extends RpcService, I extends R> ObjectRegistration<I> registerRpcImplementation(Class<R> type, I implementation,
        Set<InstanceIdentifier<?>> paths);

    Registration registerRpcImplementation(Rpc<?, ?> implementation);

    Registration registerRpcImplementation(Rpc<?, ?> implementation, Set<InstanceIdentifier<?>> paths);

    Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations);

    Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations,
        Set<InstanceIdentifier<?>> paths);
}
