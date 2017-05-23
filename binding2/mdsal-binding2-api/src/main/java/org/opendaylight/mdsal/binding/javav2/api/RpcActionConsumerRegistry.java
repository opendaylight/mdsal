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

@Beta
public interface RpcActionConsumerRegistry extends BindingService {

    <T extends Rpc> T getRpcService(Class<T> serviceInterface);

    <T extends Action> T getActionService(Class<T> serviceInterface);

    <T extends ListAction> T getListActionService(Class<T> serviceInterface);

}
