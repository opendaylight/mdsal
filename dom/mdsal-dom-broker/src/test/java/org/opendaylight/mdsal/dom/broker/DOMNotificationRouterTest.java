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

import com.google.common.collect.Multimap;
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
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class DOMNotificationRouterTest {

    @Test
    public void create() throws Exception {
        assertNotNull(DOMNotificationRouter.create(1024));
    }

    @Test
    public void registerNotificationListener() {
        final var domNotificationRouter = new DOMNotificationRouter(1024);
        final var domNotificationListener = mock(DOMNotificationListener.class);

        domNotificationRouter.registerNotificationListener(domNotificationListener,
            List.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif1"))));
        assertEquals(1, domNotificationRouter.listeners().size());

        domNotificationRouter.registerNotificationListener(domNotificationListener,
            List.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif2")),
                Absolute.of(QName.create("urn:opendaylight:test-listener", "notif3"))));
        assertEquals(3, domNotificationRouter.listeners().size());
    }

    @Test
    public void registerNotificationListeners() {
        final var domNotificationRouter = new DOMNotificationRouter(1024);
        final var domNotificationListener1 = mock(DOMNotificationListener.class);
        final var domNotificationListener2 = mock(DOMNotificationListener.class);

        domNotificationRouter.registerNotificationListeners(
            Map.of(Absolute.of(QName.create("urn:opendaylight:test-listener", "notif1")), domNotificationListener1,
                Absolute.of(QName.create("urn:opendaylight:test-listener", "notif2")), domNotificationListener2));
        assertEquals(2, domNotificationRouter.listeners().size());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Test
    public void complexTest() throws Exception {
        final DOMNotificationSubscriptionListener domNotificationSubscriptionListener =
                mock(DOMNotificationSubscriptionListener.class);
        doNothing().when(domNotificationSubscriptionListener).onSubscriptionChanged(any());

        final CountDownLatch latch = new CountDownLatch(1);
        final DOMNotificationListener domNotificationListener = new TestListener(latch);
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1024);

        Multimap<Absolute, ?> listeners = domNotificationRouter.listeners();

        assertTrue(listeners.isEmpty());
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener,
            Absolute.of(TestModel.TEST_QNAME)));
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener,
            Absolute.of(TestModel.TEST2_QNAME)));

        listeners = domNotificationRouter.listeners();

        assertFalse(listeners.isEmpty());

        ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
                domNotificationRouter.subscriptionListeners();

        assertEquals(0, subscriptionListeners.streamListeners().count());
        assertNotNull(domNotificationRouter.registerSubscriptionListener(domNotificationSubscriptionListener));

        subscriptionListeners = domNotificationRouter.subscriptionListeners();
        assertSame(domNotificationSubscriptionListener,
            subscriptionListeners.streamListeners().findAny().orElseThrow());

        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        assertNotNull(domNotificationRouter.offerNotification(domNotification));

        try {
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        } catch (Exception e) {
            // FIXME: what is the point here?!
            assertTrue(e instanceof UnsupportedOperationException);
        }

        assertNotNull(domNotificationRouter.putNotification(domNotification));
    }

    @Test
    public void offerNotification() throws Exception {
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1024);
        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();
        assertNotNull(domNotificationRouter.putNotification(domNotification));
        assertNotNull(domNotificationRouter.offerNotification(domNotification));
        assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
    }

    @Test
    public void testOfferNotificationWithBlocking() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final TestListener testListener = new TestListener(latch);
        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(Absolute.of(TestModel.TEST_QNAME)).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        try (TestRouter testRouter = new TestRouter(1)) {
            assertNotNull(testRouter.registerNotificationListener(testListener, Absolute.of(TestModel.TEST_QNAME)));
            assertNotNull(testRouter.registerNotificationListener(testListener, Absolute.of(TestModel.TEST2_QNAME)));

            assertNotEquals(DOMNotificationPublishService.REJECTED,
                testRouter.offerNotification(domNotification, 3, TimeUnit.SECONDS));
            assertTrue("Listener was not notified", latch.await(5, TimeUnit.SECONDS));
            assertEquals("Received notifications", 1, testListener.getReceivedNotifications().size());

            assertEquals(DOMNotificationPublishService.REJECTED,
                    testRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
            assertEquals("Received notifications", 1, testListener.getReceivedNotifications().size());
        }
    }

    @Test
    public void close() throws Exception {
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1024);
        final ExecutorService executor = domNotificationRouter.executor();
        final ExecutorService observer = domNotificationRouter.observer();

        assertFalse(executor.isShutdown());
        assertFalse(observer.isShutdown());
        domNotificationRouter.close();
        assertTrue(executor.isShutdown());
        assertTrue(observer.isShutdown());
    }

    private static class TestListener implements DOMNotificationListener {
        private final CountDownLatch latch;
        private final List<DOMNotification>  receivedNotifications = new ArrayList<>();

        TestListener(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onNotification(final DOMNotification notification) {
            receivedNotifications.add(notification);
            latch.countDown();
        }

        public List<DOMNotification> getReceivedNotifications() {
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
                final Collection<Reg<?>> subscribers) {
            if (triggerRejected) {
                return REJECTED;
            }

            triggerRejected = true;
            return super.publish(notification, subscribers);
        }

        @Override
        public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
                throws InterruptedException {
            Thread.sleep(2000);
            return super.putNotification(notification);
        }
    }
}
