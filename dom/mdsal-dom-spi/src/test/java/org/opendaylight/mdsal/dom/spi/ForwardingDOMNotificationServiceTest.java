/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.Registration;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMNotificationServiceTest {
    @Mock
    private DOMNotificationService delegate;
    @Mock
    private DOMNotificationListener listener;
    @Mock
    private Registration registration;
    @Spy
    private ForwardingDOMNotificationService service;

    @BeforeEach
    void beforeEach() {
        doReturn(delegate).when(service).delegate();
    }

    @Test
    void registerAllForwards() {
        doReturn(registration).when(delegate).registerNotificationListener(listener);
        assertSame(registration, service.registerNotificationListener(listener));
    }

    @Test
    void registerSetForwards() {
        doReturn(registration).when(delegate).registerNotificationListener(listener, Set.of());
        assertSame(registration, service.registerNotificationListener(listener, Set.of()));
    }
}