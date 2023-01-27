/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;

public class NotificationListenerInvokerTest {

    @Test
    public void fromWithExceptionTest() {
        final var cause = assertThrows(UncheckedExecutionException.class,
            () -> NotificationListenerInvoker.from(TestPrivateInterface.class))
            .getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
    }

    @Test
    public void invokeNotification() {
        final NotificationListener notificationListener = mock(NotificationListener.class);
        final MethodHandle methodHandle = mock(MethodHandle.class);
        final NotificationListenerInvoker notificationListenerInvoker =
                new NotificationListenerInvoker(ImmutableMap.of(QName.create("test", "test"), methodHandle));

        final var ex = assertThrows(WrongMethodTypeException.class,
            () -> notificationListenerInvoker.invokeNotification(notificationListener, QName.create("test", "test"),
                null));
        assertEquals("expected null but found (NotificationListener,DataContainer)void", ex.getMessage());
    }

    private interface TestPrivateInterface extends NotificationListener, Augmentation {
        QName QNAME = QName.create("test", "test");

        void onTestNotificationInterface(TestNotificationInterface notif);
    }

    public interface TestNotificationInterface extends DataObject, Notification<TestNotificationInterface> {
        QName QNAME = QName.create("test", "test");
    }
}