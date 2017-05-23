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
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

@Beta
public interface RpcActionProviderService {

    <S extends Rpc, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
        T implementation);

    <S extends Rpc, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
        T implementation, Set<InstanceIdentifier<?>> paths);

    <S extends Action, T extends S> ObjectRegistration<T> registerActionImplementation(Class<S> type,
        T implementation);

    <S extends Action, T extends S> ObjectRegistration<T> registerActionImplementation(Class<S> type,
        T implementation, Set<InstanceIdentifier<?>> paths);

    <S extends ListAction, T extends S> ObjectRegistration<T> registerListActionImplementation(Class<S> type,
        T implementation);

    <S extends ListAction, T extends S> ObjectRegistration<T> registerListActionImplementation(Class<S> type,
        T implementation, Set<InstanceIdentifier<?>> paths);

}
