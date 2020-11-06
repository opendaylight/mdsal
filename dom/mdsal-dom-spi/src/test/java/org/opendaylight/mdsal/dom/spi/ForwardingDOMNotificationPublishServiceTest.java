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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMNotificationPublishServiceTest extends ForwardingDOMNotificationPublishService {
    @Mock(name = "domNotificationPublishService")
    public DOMNotificationPublishService domNotificationPublishService;

    @Test
    public void basicTest() throws Exception {
        final DOMNotification domNotification = mock(DOMNotification.class);

        doReturn(null).when(domNotificationPublishService).putNotification(domNotification);
        this.putNotification(domNotification);
        verify(domNotificationPublishService).putNotification(domNotification);

        doReturn(null).when(domNotificationPublishService).offerNotification(domNotification);
        this.offerNotification(domNotification);
        verify(domNotificationPublishService).offerNotification(domNotification);

        doReturn(null).when(domNotificationPublishService).offerNotification(domNotification, 1, TimeUnit.MILLISECONDS);
        this.offerNotification(domNotification, 1, TimeUnit.MILLISECONDS);
        verify(domNotificationPublishService).offerNotification(domNotification, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    protected DOMNotificationPublishService delegate() {
        return domNotificationPublishService;
    }
}