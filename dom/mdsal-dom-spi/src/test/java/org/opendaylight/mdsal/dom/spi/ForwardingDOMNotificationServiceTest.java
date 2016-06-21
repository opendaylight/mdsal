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
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;

public class ForwardingDOMNotificationServiceTest extends ForwardingDOMNotificationService {

    @Mock(name = "domNotificationService")
    private DOMNotificationService domNotificationService;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        final DOMNotificationListener domNotificationListener = mock(DOMNotificationListener.class);

        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener);
        this.registerNotificationListener(domNotificationListener);
        verify(domNotificationService).registerNotificationListener(domNotificationListener);

        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener,
                Collections.EMPTY_SET);
        this.registerNotificationListener(domNotificationListener, Collections.EMPTY_SET);
        verify(domNotificationService).registerNotificationListener(domNotificationListener, Collections.EMPTY_SET);
    }

    @Override
    protected DOMNotificationService delegate() {
        return domNotificationService;
    }
}