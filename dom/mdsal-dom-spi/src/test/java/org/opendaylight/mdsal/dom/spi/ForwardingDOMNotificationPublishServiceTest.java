/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMNotificationPublishServiceTest extends ForwardingDOMNotificationPublishService {
    @Mock(name = "domNotificationPublishService")
    private DOMNotificationPublishService domNotificationPublishService;
    @Mock
    private DOMNotification domNotification;

    @Test
    void basicTest() throws Exception {
        doReturn(null).when(domNotificationPublishService).putNotification(domNotification);
        assertNotNull(putNotification(domNotification));
        verify(domNotificationPublishService).putNotification(domNotification);

        doReturn(null).when(domNotificationPublishService).offerNotification(domNotification);
        assertNotNull(offerNotification(domNotification));
        verify(domNotificationPublishService).offerNotification(domNotification);

        doReturn(null).when(domNotificationPublishService).offerNotification(domNotification, 1, TimeUnit.MILLISECONDS);
        assertNotNull(offerNotification(domNotification, 1, TimeUnit.MILLISECONDS));
        verify(domNotificationPublishService).offerNotification(domNotification, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    protected DOMNotificationPublishService delegate() {
        return domNotificationPublishService;
    }
}