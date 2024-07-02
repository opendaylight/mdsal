/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.dom.broker.TestUtils.TEST_CHILD;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension.DemandListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class DOMNotificationRouterTest {
    @Test
    public void registerNotificationListener() {
        try (var domNotificationRouter = new DOMNotificationRouter(1024)) {
            final var domNotificationListener = mock(DOMNotificationListener.class);

            domNotificationRouter.notificationService().registerNotificationListener(domNotificationListener,
                List.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif1"))));
            assertEquals(1, domNotificationRouter.listeners().size());

            domNotificationRouter.notificationService().registerNotificationListener(domNotificationListener,
                List.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif2")),
                    Absolute.of(QName.create("urn:opendaylight:test-listener", "notif3"))));
            assertEquals(3, domNotificationRouter.listeners().size());
        }
    }

    @Test
    public void registerNotificationListeners() {
        try (var domNotificationRouter = new DOMNotificationRouter(1024)) {
            final var domNotificationListener1 = mock(DOMNotificationListener.class);
            final var domNotificationListener2 = mock(DOMNotificationListener.class);

            domNotificationRouter.notificationService().registerNotificationListeners(
                Map.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif1")), domNotificationListener1,
                    Absolute.of(QName.create("urn:opendaylight:test-listener", "notif2")), domNotificationListener2));
            assertEquals(2, domNotificationRouter.listeners().size());
        }
    }

    @Test
    public void complexTest() throws Exception {
        final var demandListener = mock(DemandListener.class);
        doNothing().when(demandListener).onDemandUpdated(any());

        final var latch = new CountDownLatch(1);
        final var domNotificationListener = new TestListener(latch);
        final var domNotificationRouter = new DOMNotificationRouter(1024);
        final var notifService = domNotificationRouter.notificationService();
        final var notifPubService = domNotificationRouter.notificationPublishService();
        final var demandExt = notifPubService.extension(DOMNotificationPublishDemandExtension.class);
        assertNotNull(demandExt);

        var listeners = domNotificationRouter.listeners();

        assertTrue(listeners.isEmpty());
        assertNotNull(notifService.registerNotificationListener(domNotificationListener,
            List.of(Absolute.of(TestModel.TEST_QNAME))));
        assertNotNull(notifService.registerNotificationListener(domNotificationListener,
            List.of(Absolute.of(TestModel.TEST2_QNAME))));

        listeners = domNotificationRouter.listeners();

        assertFalse(listeners.isEmpty());

        assertEquals(0, domNotificationRouter.demandListeners().streamObjects().count());

        assertNotNull(demandExt.registerDemandListener(demandListener));

        assertSame(demandListener, domNotificationRouter.demandListeners().streamObjects().findAny().orElseThrow());

        final var domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        assertNotNull(notifPubService.offerNotification(domNotification));

        assertNotNull(notifPubService.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        assertNotNull(notifPubService.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        assertNotNull(notifPubService.putNotification(domNotification));
    }

    @Test
    public void offerNotification() throws Exception {
        try (var domNotificationRouter = new DOMNotificationRouter(1024)) {
            final var domNotification = mock(DOMNotification.class);
            doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
            doReturn(TEST_CHILD).when(domNotification).getBody();

            final var notifPubService = domNotificationRouter.notificationPublishService();
            assertNotNull(notifPubService.putNotification(domNotification));
            assertNotNull(notifPubService.offerNotification(domNotification));
            assertNotNull(notifPubService.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        }
    }

    @Test
    public void testOfferNotificationWithBlocking() throws Exception {
        final var latch = new CountDownLatch(1);
        final var testListener = new TestListener(latch);
        final var domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        try (var testRouter = new TestRouter(1)) {
            final var notifService = testRouter.notificationService();
            final var notifPubService = testRouter.notificationPublishService();

            assertNotNull(notifService.registerNotificationListener(testListener,
                List.of(Absolute.of(TestModel.TEST_QNAME))));
            assertNotNull(notifService.registerNotificationListener(testListener,
                List.of(Absolute.of(TestModel.TEST2_QNAME))));

            assertNotEquals(DOMNotificationPublishService.REJECTED,
                notifPubService.offerNotification(domNotification, 3, TimeUnit.SECONDS));
            assertTrue("Listener was not notified", latch.await(5, TimeUnit.SECONDS));
            assertEquals("Received notifications", 1, testListener.getReceivedNotifications().size());

            assertEquals(DOMNotificationPublishService.REJECTED,
                notifPubService.offerNotification(domNotification, 1, TimeUnit.SECONDS));
            assertEquals("Received notifications", 1, testListener.getReceivedNotifications().size());
        }
    }

    @Test
    public void close() {
        final ExecutorService executor;
        final ExecutorService observer;

        try (var domNotificationRouter = new DOMNotificationRouter(1024)) {
            executor = domNotificationRouter.executor();
            observer = domNotificationRouter.observer();
            assertFalse(executor.isShutdown());
            assertFalse(observer.isShutdown());
        }
        assertTrue(executor.isShutdown());
        assertTrue(observer.isShutdown());
    }

    private static class TestListener implements DOMNotificationListener {
        private final List<DOMNotification>  receivedNotifications = new ArrayList<>();
        private final CountDownLatch latch;

        TestListener(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onNotification(final DOMNotification notification) {
            receivedNotifications.add(notification);
            latch.countDown();
        }

        List<DOMNotification> getReceivedNotifications() {
            return receivedNotifications;
        }
    }

    private static class TestRouter extends DOMNotificationRouter {

        private boolean triggerRejected = false;

        TestRouter(final int queueDepth) {
            super(queueDepth);
        }

        @Override
        ListenableFuture<? extends Object> publish(final DOMNotification notification,
                final Collection<Reg> subscribers) {
            if (triggerRejected) {
                return DOMNotificationPublishService.REJECTED;
            }

            triggerRejected = true;
            return super.publish(notification, subscribers);
        }

        @Override
        public ListenableFuture<? extends Object> putNotificationImpl(final DOMNotification notification)
                throws InterruptedException {
            Thread.sleep(2000);
            return super.putNotificationImpl(notification);
        }
    }
}
