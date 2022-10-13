/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

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
    @Deprecated(since = "11.0.0", forRemoval = true)
    <R extends RpcService, T extends R> ObjectRegistration<T> registerRpcImplementation(Class<R> type,
        T implementation);

    @Deprecated(since = "11.0.0", forRemoval = true)
    <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
        T implementation, Set<InstanceIdentifier<?>> paths);

    Registration registerRpcImplementation(Rpc<?, ?> implementation);

    Registration registerRpcImplementation(Rpc<?, ?> implementation, Set<InstanceIdentifier<?>> paths);
}
