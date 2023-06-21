/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractNotificationBrokerTest;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.OpendaylightMdsalBindingTestListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TwoLevelListChangedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

public class ForwardedNotificationAdapterTest extends AbstractNotificationBrokerTest {
    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(TwoLevelListChanged.class));
    }

    @Test
    public void testPutSubscription() throws InterruptedException {
        final var listener = new TestNotifListener(1);
        try (var reg = getNotificationService().registerNotificationListener(listener)) {
            assertPutSubscription(listener);
        }
    }

    @Test
    public void testPutSubscriptionSimple() throws InterruptedException {
        final var listener = new SimpleNotifListener(1);
        try (var reg = getNotificationService().registerListener(TwoLevelListChanged.class, listener)) {
            assertPutSubscription(listener);
        }
    }

    private void assertPutSubscription(final AbstractNotifListener listener) throws InterruptedException {
        final var testData = createTestData();
        getNotificationPublishService().putNotification(testData);

        final var received = listener.awaitNotifications();
        assertEquals(1, received.size());
        assertSame(testData, received.get(0));
    }

    @Test
    public void testOfferSubscription() throws InterruptedException {
        final var listener = new TestNotifListener(1);
        try (var reg = getNotificationService().registerNotificationListener(listener)) {
            assertOfferNotification(listener);
        }
    }

    @Test
    public void testOfferSubscriptionSimple() throws InterruptedException {
        final var listener = new SimpleNotifListener(1);
        try (var reg = getNotificationService().registerListener(TwoLevelListChanged.class, listener)) {
            assertOfferNotification(listener);
        }
    }

    private void assertOfferNotification(final AbstractNotifListener listener) throws InterruptedException {
        final var testData = createTestData();

        try {
            getNotificationPublishService().offerNotification(testData).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new AssertionError("Notification should be delivered", e);
        }

        final var received = listener.awaitNotifications();
        assertEquals(1, received.size());
        assertSame(testData, received.get(0));
    }

    @Test
    public void testOfferTimedNotification() throws InterruptedException {
        final var listener = new TestNotifListener(1);
        try (var reg = getNotificationService().registerNotificationListener(listener)) {
            assertOfferTimedNotification(listener);
        }
    }

    @Test
    public void testOfferTimedNotificationSimple() throws InterruptedException {
        final var listener = new SimpleNotifListener(1);
        try (var reg = getNotificationService().registerListener(TwoLevelListChanged.class, listener)) {
            assertOfferTimedNotification(listener);
        }
    }

    private void assertOfferTimedNotification(final AbstractNotifListener listener) throws InterruptedException {
        final var testData = createTestData();

        assertNotSame(NotificationPublishService.REJECTED,
            getNotificationPublishService().offerNotification(testData, 5, TimeUnit.SECONDS));

        final var received = listener.awaitNotifications();
        assertEquals(1, received.size());
        assertSame(testData, received.get(0));
    }


    private static @NonNull TwoLevelListChanged createTestData() {
        return new TwoLevelListChangedBuilder()
            .setTopLevelList(BindingMap.of(new TopLevelListBuilder().withKey(new TopLevelListKey("test")).build()))
            .build();
    }

    private abstract static class AbstractNotifListener {
        private final List<TwoLevelListChanged> receivedNotifications = new ArrayList<>();
        private final CountDownLatch latch;

        AbstractNotifListener(final int expectedCount) {
            latch = new CountDownLatch(expectedCount);
        }

        final void receiveNotification(final TwoLevelListChanged notification) {
            receivedNotifications.add(notification);
            latch.countDown();
        }

        final List<TwoLevelListChanged> awaitNotifications() throws InterruptedException {
            latch.await();
            return receivedNotifications;
        }
    }

    private static class SimpleNotifListener extends AbstractNotifListener implements Listener<TwoLevelListChanged> {
        SimpleNotifListener(final int expectedCount) {
            super(expectedCount);
        }

        @Override
        public void onNotification(final TwoLevelListChanged notification) {
            receiveNotification(notification);
        }
    }

    private static class TestNotifListener extends AbstractNotifListener
            implements OpendaylightMdsalBindingTestListener {
        TestNotifListener(final int expectedCount) {
            super(expectedCount);
        }

        @Override
        public void onTwoLevelListChanged(final TwoLevelListChanged notification) {
            receiveNotification(notification);
        }
    }
}
