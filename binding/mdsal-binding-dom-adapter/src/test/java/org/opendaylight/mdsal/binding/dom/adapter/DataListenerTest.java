/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTest;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTestBuilder;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.Item;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.ItemBuilder;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.ItemKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class DataListenerTest extends AbstractDataBrokerTest {
    DataBroker dataBroker;

    DataListener<Item> listener;

    DataChangeListener<Item> changeListener;

    @Before
    public void setUp() {
        dataBroker = getDataBroker();
        listener = mock(DataListener.class);
        changeListener = mock(DataChangeListener.class);
    }

    @Test
    public void testThrowExceptionOnRegister() {
        final InstanceIdentifier<Item> instanceIdentifier = InstanceIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class).build();
        final DataTreeIdentifier<Item> itemsDataTreeIdentifier = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL,
            instanceIdentifier);

        final Throwable dataListenerException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataListener(itemsDataTreeIdentifier, listener));
        assertTrue(dataListenerException.getMessage().contains("Cannot register listener for wildcard"));

        final Throwable dataListenerChangeException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataChangeListener(itemsDataTreeIdentifier, changeListener));
        assertTrue(dataListenerChangeException.getMessage().contains("Cannot register listener for wildcard"));
    }

    @Test
    public void testRegisterDataListener() {
        final Item item = writeItems();
        final InstanceIdentifier<Item> instanceIdentifier = InstanceIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class, new ItemKey(item.key())).build();

        dataBroker.registerDataListener(
            DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, instanceIdentifier), listener);

        verify(listener, timeout(100)).dataChangedTo(item);
    }

    @Test
    public void testRegisterDataChangeListener() {
        final Item item = writeItems();
        final InstanceIdentifier<Item> instanceIdentifier = InstanceIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class, new ItemKey(item.key())).build();

        dataBroker.registerDataChangeListener(
            DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, instanceIdentifier), changeListener);

        verify(changeListener, timeout(100)).dataChanged(null, item);
    }

    private Item writeItems() {
        final WriteTransaction writeTransaction = getDataBroker().newWriteOnlyTransaction();
        final Item wildcardItem = new ItemBuilder().setText("name").setNumber(Uint32.valueOf(43)).build();
        final RegisterListenerTestBuilder builder = new RegisterListenerTestBuilder().setItem(
            BindingMap.of(wildcardItem));
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.builder(
            RegisterListenerTest.class).build(), builder.build());
        assertCommit(writeTransaction.commit());
        return wildcardItem;
    }
}
