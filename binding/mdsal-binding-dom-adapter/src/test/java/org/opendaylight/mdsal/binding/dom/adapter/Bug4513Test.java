/*
 * Copyright (c) 2015 Cisco Systems, Inc., Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.ListenerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.ListenerTestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.listener.test.ListItem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.listener.rev150825.listener.test.ListItemBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Regression test suite for https://bugs.opendaylight.org/show_bug.cgi?id=4513 - Change event is empty when
 * Homogeneous composite key is used homogeneous composite key is used.
 */
public class Bug4513Test extends AbstractDataBrokerTest {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testDataTreeChangeListener() {
        DataBroker dataBroker = getDataBroker();

        DataTreeChangeListener<ListItem> listener = mock(DataTreeChangeListener.class);
        InstanceIdentifier<ListItem> wildCard = InstanceIdentifier.builder(ListenerTest.class)
                .child(ListItem.class).build();
        ListenerRegistration<DataTreeChangeListener<ListItem>> reg = dataBroker.registerDataTreeChangeListener(
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, wildCard), listener);

        final ListItem item = writeListItem();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(listener, timeout(100)).onDataTreeChanged(captor.capture());

        List<DataTreeModification<ListItem>> mods = captor.getValue();
        assertEquals("ListItem", item, mods.iterator().next().getRootNode().getDataAfter());
    }

    private ListItem writeListItem() {
        WriteTransaction writeTransaction = getDataBroker().newWriteOnlyTransaction();
        final ListItem item = new ListItemBuilder().setSip("name").setOp(Uint32.valueOf(43)).build();
        ListenerTestBuilder builder = new ListenerTestBuilder().setListItem(Map.of(item.key(), item));
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.builder(
                ListenerTest.class).build(), builder.build());
        assertCommit(writeTransaction.commit());
        return item;
    }
}
