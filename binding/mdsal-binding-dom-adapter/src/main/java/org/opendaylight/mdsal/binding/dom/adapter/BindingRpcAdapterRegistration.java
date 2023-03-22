/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.RpcService;

@Deprecated(since = "11.0.0", forRemoval = true)
class BindingRpcAdapterRegistration<T extends RpcService> extends AbstractObjectRegistration<T> {
    private final Registration reg;

    BindingRpcAdapterRegistration(final T instance, final Registration reg) {
        super(instance);
        this.reg = reg;
    }

    @Override
    protected void removeRegistration() {
        reg.close();
    }
}