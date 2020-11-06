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

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMNotificationServiceTest extends ForwardingDOMNotificationService {
    @Mock(name = "domNotificationService")
    public DOMNotificationService domNotificationService;

    @Test
    public void basicTest() throws Exception {
        final DOMNotificationListener domNotificationListener = mock(DOMNotificationListener.class);

        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener);
        this.registerNotificationListener(domNotificationListener);
        verify(domNotificationService).registerNotificationListener(domNotificationListener);

        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener,
                Collections.emptySet());
        this.registerNotificationListener(domNotificationListener, Collections.emptySet());
        verify(domNotificationService).registerNotificationListener(domNotificationListener, Collections.emptySet());
    }

    @Override
    protected DOMNotificationService delegate() {
        return domNotificationService;
    }
}