/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.yangtools.yang.binding.RpcService;

@Deprecated(since = "11.0.0", forRemoval = true)
public class BindingRpcAdapterRegistrationTest {
    @Test
    public void removeRegistrationTest() throws Exception {
        final RpcService rpcService = mock(RpcService.class);
        final DOMRpcImplementationRegistration<?> domRpcImplementationRegistration =
                mock(DOMRpcImplementationRegistration.class);
        final BindingRpcAdapterRegistration<?> bindingRpcAdapterRegistration =
                new BindingRpcAdapterRegistration<>(rpcService, domRpcImplementationRegistration);
        doNothing().when(domRpcImplementationRegistration).close();
        bindingRpcAdapterRegistration.close();
        verify(domRpcImplementationRegistration).close();
    }
}