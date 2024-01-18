/*
 * Copyright (c) 2015 Cisco Systems, Inc., Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.ListenerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.ListenerTestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.listener.test.ListItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.listener.test.ListItemBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Regression test suite for https://bugs.opendaylight.org/show_bug.cgi?id=4513 - Change event is empty when
 * Homogeneous composite key is used homogeneous composite key is used.
 */
@ExtendWith(MockitoExtension.class)
class Bug4513Test extends AbstractDataBrokerTest {
    @Mock
    private DataTreeChangeListener<ListItem> listener;
    @Captor
    private ArgumentCaptor<List<DataTreeModification<ListItem>>> captor;

    @Test
    void testDataTreeChangeListener() {
        final var dataBroker = getDataBroker();

        final var wildCard = InstanceIdentifier.builder(ListenerTest.class).child(ListItem.class).build();
        try (var reg = dataBroker.registerDataTreeChangeListener(
                DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, wildCard), listener)) {
            final var item = writeListItem();

            verify(listener, timeout(100)).onDataTreeChanged(captor.capture());

            final var mods = captor.getValue();
            assertEquals(1, mods.size());
            assertEquals(item, mods.get(0).getRootNode().dataAfter());
        }
    }

    private ListItem writeListItem() {
        final var writeTransaction = getDataBroker().newWriteOnlyTransaction();
        final var item = new ListItemBuilder().setSip("name").setOp(Uint32.valueOf(43)).build();
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(ListenerTest.class),
            new ListenerTestBuilder().setListItem(BindingMap.of(item)).build());
        assertCommit(writeTransaction.commit());
        return item;
    }
}
