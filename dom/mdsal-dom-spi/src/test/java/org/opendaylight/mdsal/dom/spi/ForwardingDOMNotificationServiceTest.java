/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMNotificationServiceTest extends ForwardingDOMNotificationService {
    @Mock(name = "domNotificationService")
    private DOMNotificationService domNotificationService;
    @Mock
    private DOMNotificationListener domNotificationListener;

    @Test
    void basicTest() {
        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener);
        registerNotificationListener(domNotificationListener);
        verify(domNotificationService).registerNotificationListener(domNotificationListener);

        doReturn(null).when(domNotificationService).registerNotificationListener(domNotificationListener, Set.of());
        registerNotificationListener(domNotificationListener, Set.of());
        verify(domNotificationService).registerNotificationListener(domNotificationListener, Set.of());
    }

    @Override
    protected DOMNotificationService delegate() {
        return domNotificationService;
    }
}