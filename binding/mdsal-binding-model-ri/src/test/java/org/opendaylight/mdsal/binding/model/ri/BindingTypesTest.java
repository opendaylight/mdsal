/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.mdsal.binding.model.ri.Types.typeForClass;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class BindingTypesTest {
    @Test
    public void staticBindingTypesTest() {
        assertEquals(typeForClass(Augmentable.class), BindingTypes.AUGMENTABLE);
        assertEquals(typeForClass(Augmentation.class), BindingTypes.AUGMENTATION);
        assertEquals(typeForClass(BaseIdentity.class), BindingTypes.BASE_IDENTITY);
        assertEquals(typeForClass(DataObject.class), BindingTypes.DATA_OBJECT);
        assertEquals(typeForClass(DataRoot.class), BindingTypes.DATA_ROOT);
        assertEquals(typeForClass(KeyAware.class), BindingTypes.KEY_AWARE);
        assertEquals(typeForClass(Key.class), BindingTypes.KEY);
        assertEquals(typeForClass(NotificationListener.class), BindingTypes.NOTIFICATION_LISTENER);
        assertEquals(typeForClass(RpcService.class), BindingTypes.RPC_SERVICE);
    }

    @Test
    public void testAugmentableNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentable(null));
    }

    @Test
    public void testChildOfNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.childOf(null));
    }

    @Test
    public void testAugmentable() {
        ParameterizedType augmentableType = BindingTypes.augmentable(Types.objectType());
        assertEquals("Augmentable", augmentableType.getName());
    }

    @Test
    public void testChildOf() {
        assertNotNull(BindingTypes.childOf(Types.objectType()));
    }

    @Test
    public void testAugmentationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.augmentation(null));
    }

    @Test
    public void testAugmentation() {
        final var augmentationType = BindingTypes.augmentation(Types.objectType());
        assertEquals("Augmentation", augmentationType.getName());
    }

    @Test
    public void testNotificationNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.notification(null));
    }

    @Test
    public void testNotification() {
        final var notificationType = BindingTypes.notification(Types.objectType());
        assertEquals(Types.typeForClass(Notification.class), notificationType.getRawType());
        assertArrayEquals(new Object[] { Types.objectType() }, notificationType.getActualTypeArguments());
    }

    @Test
    public void testInstanceIdentifier() {
        final var iidType = BindingTypes.instanceIdentifier(Types.objectType());
        assertEquals(Types.typeForClass(BindingDataObjectIdentifier.class), iidType.getRawType());
        assertArrayEquals(new Object[] { Types.objectType() }, iidType.getActualTypeArguments());
    }
}