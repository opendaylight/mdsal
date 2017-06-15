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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.WaitStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DOMNotificationRouterTest extends TestUtils {

    private static final WaitStrategy DEFAULT_STRATEGY = PhasedBackoffWaitStrategy.withLock(
            1L, 30L, TimeUnit.MILLISECONDS);

    @Test
    public void create() throws Exception {
        assertNotNull(DOMNotificationRouter.create(1,1,1,TimeUnit.SECONDS));
        assertNotNull(DOMNotificationRouter.create(1));
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Test
    public void complexTest() throws Exception {
        final DOMNotificationSubscriptionListener domNotificationSubscriptionListener =
                mock(DOMNotificationSubscriptionListener.class);
        final DOMNotificationListener domNotificationListener = new TestListener();
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1);

        Multimap<SchemaPath, ?> listeners = domNotificationRouter.listeners();

        assertTrue(listeners.isEmpty());
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener, SchemaPath.ROOT));
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener, SchemaPath.SAME));

        listeners = domNotificationRouter.listeners();

        assertFalse(listeners.isEmpty());

        ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
                domNotificationRouter.subscriptionListeners();

        assertFalse(subscriptionListeners.iterator().hasNext());
        assertNotNull(domNotificationRouter.registerSubscriptionListener(domNotificationSubscriptionListener));

        subscriptionListeners = domNotificationRouter.subscriptionListeners();
        assertTrue(subscriptionListeners.iterator().hasNext());

        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(SchemaPath.ROOT).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        assertNotNull(domNotificationRouter.offerNotification(domNotification));

        try {
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        assertNotNull(domNotificationRouter.putNotification(domNotification));
    }

    @Test
    public void offerNotification() throws Exception {
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1);
        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn(SchemaPath.ROOT).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();
        assertNotNull(domNotificationRouter.putNotification(domNotification));
        assertNotNull(domNotificationRouter.offerNotification(domNotification));
        assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
    }

    @Test
    public void offerNotificationComplex() throws Exception {
        final DOMNotificationSubscriptionListener domNotificationSubscriptionListener =
                mock(DOMNotificationSubscriptionListener.class);
        final TestListener testListener = new TestListener();
        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(SchemaPath.ROOT).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();
        final TestRouter testRouter = TestRouter.create(1);

        Multimap<SchemaPath, ?> listeners = testRouter.listeners();

        assertTrue(listeners.isEmpty());
        assertNotNull(testRouter.registerNotificationListener(testListener, SchemaPath.ROOT));
        assertNotNull(testRouter.registerNotificationListener(testListener, SchemaPath.SAME));

        listeners = testRouter.listeners();

        assertFalse(listeners.isEmpty());

        ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
                testRouter.subscriptionListeners();

        assertFalse(subscriptionListeners.iterator().hasNext());
        assertNotNull(testRouter.registerSubscriptionListener(domNotificationSubscriptionListener));

        subscriptionListeners = testRouter.subscriptionListeners();
        assertTrue(subscriptionListeners.iterator().hasNext());

        assertEquals(DOMNotificationPublishService.REJECTED,
                    testRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));

        assertTrue(testListener.getReceivedNotifications().size() == 0);
        assertNotNull(testRouter.putNotification(domNotification));
    }

    @Test
    public void close() throws Exception {
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1);

        final ExecutorService executor = domNotificationRouter.executor();

        assertFalse(executor.isShutdown());
        domNotificationRouter.close();
        assertTrue(executor.isShutdown());
    }

    private static class TestListener implements DOMNotificationListener {
        private final List<DOMNotification>  receivedNotifications = new ArrayList<>();

        @Override
        public void onNotification(@Nonnull final DOMNotification notification) {
            receivedNotifications.add(notification);
        }

        public List<DOMNotification> getReceivedNotifications() {
            return receivedNotifications;
        }
    }

    private  static class TestRouter extends DOMNotificationRouter {

        TestRouter(ExecutorService executor, int queueDepth, WaitStrategy strategy) {
            super(executor, queueDepth, strategy);
        }

        public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
                throws InterruptedException {
            Thread.sleep(10000);
            return super.putNotification(notification);
        }

        public static TestRouter create(int queueDepth) {
            final ExecutorService executor = Executors.newCachedThreadPool();

            return new TestRouter(executor, queueDepth, DEFAULT_STRATEGY);
        }
    }

}
