/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.mdsal813.norev.RegisterListenerTest;
import org.opendaylight.yang.gen.v1.mdsal813.norev.register.listener.test.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class DataTreeChangeServiceWildcardedTest {
    @Mock
    private DataBroker dataBroker;
    @Mock
    private DataListener<Item> listener;
    @Mock
    private DataChangeListener<Item> changeListener;

    @Test
    void testThrowExceptionOnRegister() {
        final var itemIID = InstanceIdentifier.builder(RegisterListenerTest.class).child(Item.class).build();
        final var itemDTI = DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, itemIID);

        doCallRealMethod().when(dataBroker).registerDataListener(any(), any());
        final var dataListenerException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataListener(itemDTI, listener));
        assertThat(dataListenerException.getMessage(), startsWith("Cannot register listener for wildcard"));

        doCallRealMethod().when(dataBroker).registerDataChangeListener(any(), any());
        final var dataListenerChangeException = assertThrows(IllegalArgumentException.class,
            () -> dataBroker.registerDataChangeListener(itemDTI, changeListener));
        assertThat(dataListenerChangeException.getMessage(), startsWith("Cannot register listener for wildcard"));
    }
}
