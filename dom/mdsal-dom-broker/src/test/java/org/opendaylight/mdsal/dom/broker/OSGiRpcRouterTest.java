/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class OSGiRpcRouterTest {
    @Mock
    public DOMRpcRouterServices router;

    @Test
    public void testActionProvider() {
        final DOMActionProviderService delegate = mock(DOMActionProviderService.class);
        doReturn(delegate).when(router).getActionProviderService();

        new OSGiDOMActionProviderService(router);
        // FIXME: invoke something to test delegate()
    }

    @Test
    public void testAction() {
        final DOMActionService delegate = mock(DOMActionService.class);
        doReturn(delegate).when(router).getActionService();

        new OSGiDOMActionService(router);
        // FIXME: invoke something to test delegate()
    }

    @Test
    public void testRpcProvider() {
        final DOMRpcProviderService delegate = mock(DOMRpcProviderService.class);
        doReturn(delegate).when(router).getRpcProviderService();

        new OSGiDOMRpcProviderService(router);
        // FIXME: invoke something to test delegate()
    }

    @Test
    public void testRpc() {
        final DOMRpcService delegate = mock(DOMRpcService.class);
        doReturn(delegate).when(router).getRpcService();

        new OSGiDOMRpcService(router);
        // FIXME: invoke something to test delegate()
    }
}
