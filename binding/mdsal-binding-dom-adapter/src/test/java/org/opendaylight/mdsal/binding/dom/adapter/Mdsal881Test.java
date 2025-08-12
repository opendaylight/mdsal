/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService.DemandMonitor;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.OutOfPixieDustNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChanged;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.concepts.Registration;

@ExtendWith(MockitoExtension.class)
class Mdsal881Test {
    private static CurrentAdapterSerializer SERIALIZER;

    @Mock
    private DemandMonitor monitor;
    @Mock
    private Registration registration;
    @Mock
    private Listener<TwoLevelListChanged> tllListener;
    @Mock
    private Listener<OutOfPixieDustNotification> pdListener;

    private DOMNotificationRouter router;
    private NotificationService notificationService;
    private NotificationPublishService publishService;

    @BeforeAll
    static void beforeAll() {
        SERIALIZER = new CurrentAdapterSerializer(new BindingCodecContext(
            BindingRuntimeHelpers.createRuntimeContext()));
    }

    @AfterAll
    static void afterAll() {
        SERIALIZER = null;
    }

    @BeforeEach
    void beforeEach() {
        router = new DOMNotificationRouter(100);
        notificationService = new BindingDOMNotificationServiceAdapter(() -> SERIALIZER, router.notificationService());
        publishService = new BindingDOMNotificationPublishServiceAdapter(() -> SERIALIZER,
            router.notificationPublishService());
    }

    @AfterEach
    void afterEach() {
        router.close();
    }

    @Test
    void subscriberTriggersDemand() {
        doReturn(registration).when(monitor).demandEncountered();
        doNothing().when(registration).close();

        try (var monitorReg = publishService.registerDemandMonitor(TwoLevelListChanged.class, monitor)) {
            assertNotNull(monitorReg);
            verifyNoInteractions(monitor);

            try (var listenerReg = notificationService.registerListener(TwoLevelListChanged.class, tllListener)) {
                assertNotNull(listenerReg);
                verify(monitor, timeout(100)).demandEncountered();
                verifyNoInteractions(registration);
            }

            verify(registration, timeout(100)).close();
        }

        verifyNoInteractions(tllListener);
    }

    @Test
    void differentSubscriberDoesNotTriggerDemand() {
        try (var monitorReg = publishService.registerDemandMonitor(TwoLevelListChanged.class, monitor)) {
            assertNotNull(monitorReg);

            try (var listenerReg = notificationService.registerListener(OutOfPixieDustNotification.class, pdListener)) {
                assertNotNull(listenerReg);
            }
        }

        verifyNoInteractions(monitor);
        verifyNoInteractions(registration);
        verifyNoInteractions(pdListener);
    }

    @Test
    void monitorRegistrationToStringIsNice() {
        try (var monitorReg = publishService.registerDemandMonitor(TwoLevelListChanged.class, monitor)) {
            assertEquals("""
                MonitorDemandListener{type=Absolute{qnames=[(urn:opendaylight:params:xml:ns:yang:mdsal:test:binding?\
                revision=2014-07-01)two-level-list-changed]}, monitor=monitor, closed=false}""",
                monitorReg.toString());
        }
    }
}
