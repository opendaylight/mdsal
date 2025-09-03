/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataChangeListener;
import org.opendaylight.mdsal.binding.api.DataListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTest;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTestBuilder;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.Item;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.ItemBuilder;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.ItemKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
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
    public void testRegisterDataListener() {
        final var item = writeItems();
        final var instanceIdentifier = DataObjectIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class, new ItemKey(item.key())).build();

        dataBroker.registerDataListener(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, listener);

        verify(listener, timeout(100)).dataChangedTo(item);
    }

    @Test
    public void testRegisterDataChangeListener() {
        final var item = writeItems();
        final var instanceIdentifier = DataObjectIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class, new ItemKey(item.key())).build();

        dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, changeListener);

        verify(changeListener, timeout(100)).dataChanged(null, item);
    }

    private Item writeItems() {
        final WriteTransaction writeTransaction = getDataBroker().newWriteOnlyTransaction();
        final Item wildcardItem = new ItemBuilder().setText("name").setNumber(Uint32.valueOf(43)).build();
        final RegisterListenerTestBuilder builder = new RegisterListenerTestBuilder().setItem(
            BindingMap.of(wildcardItem));
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION,
            DataObjectIdentifier.builder(RegisterListenerTest.class).build(), builder.build());
        assertCommit(writeTransaction.commit());
        return wildcardItem;
    }
}
