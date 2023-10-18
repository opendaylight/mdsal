/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;

class AbstractDOMRpcImplementationRegistrationTest
        extends AbstractDOMRpcImplementationRegistration<DOMRpcImplementation> {
    private static final DOMRpcImplementation DOM_RPC_IMPLEMENTATION = mock(DOMRpcImplementation.class);

    AbstractDOMRpcImplementationRegistrationTest() {
        super(DOM_RPC_IMPLEMENTATION);
    }

    @Test
    void basicTest() {
        try (var reg = new AbstractDOMRpcImplementationRegistrationTest()) {
            assertEquals(DOM_RPC_IMPLEMENTATION, reg.getInstance());
        }
    }

    @Override
    protected void removeRegistration() {
        // NOOP
    }
}