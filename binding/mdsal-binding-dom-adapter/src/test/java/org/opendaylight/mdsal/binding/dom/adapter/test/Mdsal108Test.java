/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import java.util.ArrayList;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Mdsal108Test extends AbstractDataBrokerTest {
    @Test
    public void testDelete() {
        DataBroker dataBroker = getDataBroker();
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        ArrayList<TopLevelList> list = new ArrayList<>();
        list.add(new TopLevelListBuilder().setName("name").build());
        TopBuilder builder = new TopBuilder().setTopLevelList(list);
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Top.class), builder.build());
        assertCommit(writeTransaction.commit());

        InstanceIdentifier<TopLevelList> id = InstanceIdentifier.builder(Top.class)
                .child(TopLevelList.class, new TopLevelListKey("name")).build();

        ReadWriteTransaction writeTransaction1 = dataBroker.newReadWriteTransaction();

        writeTransaction1.delete(LogicalDatastoreType.OPERATIONAL, id);
        assertCommit(writeTransaction1.commit());
        ReadWriteTransaction writeTransaction2 = dataBroker.newReadWriteTransaction();

        writeTransaction2.delete(LogicalDatastoreType.OPERATIONAL, id);
        assertCommit(writeTransaction2.commit());
    }
}
