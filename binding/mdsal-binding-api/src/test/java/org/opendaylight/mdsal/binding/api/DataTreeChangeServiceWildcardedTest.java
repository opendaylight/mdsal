/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTest;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DataTreeChangeServiceWildcardedTest {

    DataBroker dataBroker;

    DataListener<Item> listener;

    DataChangeListener<Item> changeListener;

    @Before
    public void setUp() {
        dataBroker = mock(DataBroker.class);
        listener = mock(DataListener.class);
        changeListener = mock(DataChangeListener.class);
    }

    @Test
    public void testThrowExceptionOnRegister() {
        final InstanceIdentifier<Item> instanceIdentifier = InstanceIdentifier.builder(RegisterListenerTest.class)
            .child(Item.class).build();
        final DataTreeIdentifier<Item> itemsDataTreeIdentifier = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL, instanceIdentifier);

        doCallRealMethod().when(dataBroker).registerDataListener(any(), any());
        final var dataListenerException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataListener(itemsDataTreeIdentifier, listener));
        assertTrue(dataListenerException.getMessage().contains("Cannot register listener for wildcard"));

        doCallRealMethod().when(dataBroker).registerDataChangeListener(any(), any());
        final var dataListenerChangeException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataChangeListener(itemsDataTreeIdentifier, changeListener));
        assertTrue(dataListenerChangeException.getMessage().contains("Cannot register listener for wildcard"));
    }

}
