/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DOMNotificationRouterTest extends TestUtils {

    @Test
    public void create() throws Exception {
        assertNotNull(DOMNotificationRouter.create(1,1,1,TimeUnit.SECONDS));
        assertNotNull(DOMNotificationRouter.create(1));
    }

    @Test
    public void complexTest() throws Exception {
        final DOMNotificationSubscriptionListener domNotificationSubscriptionListener =
                mock(DOMNotificationSubscriptionListener.class);
        final DOMNotificationListener domNotificationListener = new TestListener();
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1);

        final Field listenersField = DOMNotificationRouter.class.getDeclaredField("listeners");
        listenersField.setAccessible(true);

        final Field subscriptionListenersField = DOMNotificationRouter.class.getDeclaredField("subscriptionListeners");
        subscriptionListenersField.setAccessible(true);

        Multimap<SchemaPath, ListenerRegistration<? extends DOMNotificationListener>> listeners =
                (Multimap<SchemaPath, ListenerRegistration<? extends DOMNotificationListener>>)
                        listenersField.get(domNotificationRouter);

        assertTrue(listeners.isEmpty());
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener, SchemaPath.ROOT));
        assertNotNull(domNotificationRouter.registerNotificationListener(domNotificationListener, SchemaPath.SAME));

        listeners = (Multimap<SchemaPath, ListenerRegistration<? extends DOMNotificationListener>>)
                        listenersField.get(domNotificationRouter);

        assertFalse(listeners.isEmpty());

        ListenerRegistry<DOMNotificationSubscriptionListener> subscriptionListeners =
                (ListenerRegistry<DOMNotificationSubscriptionListener>)
                        subscriptionListenersField.get(domNotificationRouter);

        assertFalse(subscriptionListeners.iterator().hasNext());
        assertNotNull(domNotificationRouter.registerSubscriptionListener(domNotificationSubscriptionListener));

        subscriptionListeners = (ListenerRegistry<DOMNotificationSubscriptionListener>)
                        subscriptionListenersField.get(domNotificationRouter);
        assertTrue(subscriptionListeners.iterator().hasNext());

        final DOMNotification domNotification = mock(DOMNotification.class);
        doReturn("test").when(domNotification).toString();
        doReturn(SchemaPath.ROOT).when(domNotification).getType();
        doReturn(TEST_CHILD).when(domNotification).getBody();

        assertNotNull(domNotificationRouter.offerNotification(domNotification));

        Exception exception = null;
        try {
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
            assertNotNull(domNotificationRouter.offerNotification(domNotification, 1, TimeUnit.SECONDS));
        } catch(UnsupportedOperationException e) {
            exception = e;
        }
        assertNotNull(exception);
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
    public void close() throws Exception {
        final DOMNotificationRouter domNotificationRouter = DOMNotificationRouter.create(1);

        final Field executorField = DOMNotificationRouter.class.getDeclaredField("executor");
        executorField.setAccessible(true);

        final ExecutorService executor = (ExecutorService) executorField.get(domNotificationRouter);

        assertFalse(executor.isShutdown());
        domNotificationRouter.close();
        assertTrue(executor.isShutdown());
    }

    private class TestListener implements DOMNotificationListener {
        @Override
        public void onNotification(@Nonnull DOMNotification notification) {}
    }
}