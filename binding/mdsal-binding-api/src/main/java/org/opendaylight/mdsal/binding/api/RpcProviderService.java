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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;

/**
 * Provides ability to registered Remote Procedure Call (RPC) service implementations. The RPCs are defined in YANG
 * models.
 */
public interface RpcProviderService extends BindingService {
    /**
     * Register an {@link Rpc} implementation.
     *
     * @param implementation implementation object
     * @return A {@link Registration} controlling unregistration
     * @throws NullPointerException if {@code implementation} is {@code null}
     */
    @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation);

    /**
     * Register an {@link Rpc} implementation on a set of datastore context paths.
     *
     * @param implementation implementation object
     * @param paths Datastore paths to service
     * @return A {@link Registration} controlling unregistration
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation, Set<InstanceIdentifier<?>> paths);

    /**
     * Register a set of {@link Rpc} implementations. Note that this method does not support registering multiple
     * implementations of the same {@link Rpc} and hence we require specifying them through a
     * {@link ClassToInstanceMap}.
     *
     * @param implementations implementation objects
     * @return A {@link Registration} controlling unregistration
     * @throws NullPointerException if {@code implementations} is {@code null}
     */
    @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations);

    /**
     * Register a set of {@link Rpc} implementations on a set of datastore context paths. Note that this method does not
     * support registering multiple implementations of the same {@link Rpc} and hence we require specifying them through
     * a {@link ClassToInstanceMap}.
     *
     * @param implementations implementation objects
     * @return A {@link Registration} controlling unregistration
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations,
        Set<InstanceIdentifier<?>> paths);
}
