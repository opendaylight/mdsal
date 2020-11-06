/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMRpcImplementationTest extends ForwardingDOMRpcImplementation {
    @Mock(name = "domRpcImplementation")
    public DOMRpcImplementation domRpcImplementation;

    @Test
    public void basicTest() throws Exception {
        final DOMRpcIdentifier domRpcIdentifier = mock(DOMRpcIdentifier.class);

        doReturn(null).when(domRpcImplementation).invokeRpc(domRpcIdentifier, null);
        this.invokeRpc(domRpcIdentifier, null);
        verify(domRpcImplementation).invokeRpc(domRpcIdentifier, null);
    }

    @Override
    protected DOMRpcImplementation delegate() {
        return domRpcImplementation;
    }
}