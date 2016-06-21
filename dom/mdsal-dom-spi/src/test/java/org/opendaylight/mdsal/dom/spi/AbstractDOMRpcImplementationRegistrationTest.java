/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;

public class AbstractDOMRpcImplementationRegistrationTest extends AbstractDOMRpcImplementationRegistration {

    private static final DOMRpcImplementation DOM_RPC_IMPLEMENTATION = mock(DOMRpcImplementation.class);

    @Test
    public void basicTest() throws Exception {
        AbstractDOMRpcImplementationRegistration abstractDOMRpcImplementationRegistration =
                new AbstractDOMRpcImplementationRegistrationTest();
        assertEquals(DOM_RPC_IMPLEMENTATION, abstractDOMRpcImplementationRegistration.getInstance());
    }

    public AbstractDOMRpcImplementationRegistrationTest() {
        super(DOM_RPC_IMPLEMENTATION);
    }

    @Override
    protected void removeRegistration() {
        // NOOP
    }
}