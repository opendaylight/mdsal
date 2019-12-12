/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.OpendaylightMdsalBindingTestListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardedNotificationAdapterTest extends AbstractTestKitTest {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardedNotificationAdapterTest.class);

    @Test
    public void testNotifSubscription() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final TwoLevelListChanged testData = createTestData();

        final TestNotifListener testNotifListener = new TestNotifListener(latch);
        final ListenerRegistration<TestNotifListener> listenerRegistration = testkit.notificationService()
                .registerNotificationListener(testNotifListener);
        testkit.notificationPublishService().putNotification(testData);

        latch.await();
        assertTrue(testNotifListener.getReceivedNotifications().size() == 1);
        assertEquals(testData, testNotifListener.getReceivedNotifications().get(0));

        listenerRegistration.close();
    }

    @Test
    public void testNotifSubscription2() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final TwoLevelListChanged testData = createTestData();

        final TestNotifListener testNotifListener = new TestNotifListener(latch);
        final ListenerRegistration<TestNotifListener> listenerRegistration = testkit.notificationService()
                .registerNotificationListener(testNotifListener);
        try {
            testkit.notificationPublishService().offerNotification(testData).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            LOG.error("Notification delivery failed", e);
            Assert.fail("notification should be delivered");
        }

        latch.await();
        assertTrue(testNotifListener.getReceivedNotifications().size() == 1);
        assertEquals(testData, testNotifListener.getReceivedNotifications().get(0));

        listenerRegistration.close();
    }

    @Test
    public void testNotifSubscription3() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final TwoLevelListChanged testData = createTestData();

        final TestNotifListener testNotifListener = new TestNotifListener(latch);
        final ListenerRegistration<TestNotifListener> listenerRegistration = testkit.notificationService()
                .registerNotificationListener(testNotifListener);
        assertNotSame(NotificationPublishService.REJECTED,
            testkit.notificationPublishService().offerNotification(testData, 5, TimeUnit.SECONDS));

        latch.await();
        assertTrue(testNotifListener.getReceivedNotifications().size() == 1);
        assertEquals(testData, testNotifListener.getReceivedNotifications().get(0));

        listenerRegistration.close();
    }

    private static class TestNotifListener implements OpendaylightMdsalBindingTestListener {
        private final List<TwoLevelListChanged> receivedNotifications = new ArrayList<>();
        private final CountDownLatch latch;

        TestNotifListener(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onTwoLevelListChanged(final TwoLevelListChanged notification) {
            receivedNotifications.add(notification);
            latch.countDown();
        }

        public List<TwoLevelListChanged> getReceivedNotifications() {
            return receivedNotifications;
        }
    }

    private static TwoLevelListChanged createTestData() {
        return new TwoLevelListChangedBuilder()
                .setTopLevelList(List.of(new TopLevelListBuilder().withKey(new TopLevelListKey("test")).build()))
                .build();
    }
}
