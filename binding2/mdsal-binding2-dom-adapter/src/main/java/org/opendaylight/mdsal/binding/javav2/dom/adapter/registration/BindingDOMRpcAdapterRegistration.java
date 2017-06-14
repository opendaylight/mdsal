/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.registration;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

/**
 * Registration of Binding - DOM RPC adapter.
 *
 * @param <T>
 *            - {@link Rpc} type
 */
@Beta
public class BindingDOMRpcAdapterRegistration<T extends Rpc<?, ?>> extends AbstractObjectRegistration<T> {

    private final DOMRpcImplementationRegistration<?> reg;

    public BindingDOMRpcAdapterRegistration(final T instance, final DOMRpcImplementationRegistration<?> reg) {
        super(instance);
        this.reg = reg;
    }

    @Override
    protected void removeRegistration() {
        reg.close();
    }
}