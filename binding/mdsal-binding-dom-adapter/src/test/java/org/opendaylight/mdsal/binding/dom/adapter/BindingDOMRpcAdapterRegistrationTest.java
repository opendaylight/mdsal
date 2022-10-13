/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.yangtools.yang.binding.RpcService;

@Deprecated(since = "11.0.0", forRemoval = true)
public class BindingDOMRpcAdapterRegistrationTest {
    @Test
    public void removeRegistration() {
        final DOMRpcImplementationRegistration<?> registration = mock(DOMRpcImplementationRegistration.class);
        final BindingDOMRpcAdapterRegistration<?> adapterReg =
                new BindingDOMRpcAdapterRegistration<>(mock(RpcService.class), registration);
        adapterReg.close();
        verify(registration).close();
    }
}