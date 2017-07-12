/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class WriteTransactionTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final TopLevelListKey TOP_LIST_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> NODE_PATH = TOP_PATH.child(TopLevelList.class, TOP_LIST_KEY);
    private static final TopLevelList NODE = new TopLevelListBuilder().setKey(TOP_LIST_KEY).build();

    @Test
    public void test() throws InterruptedException, ExecutionException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TOP_PATH, new TopBuilder().build());
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE);
        writeTx.submit().get();
    }

    @Test
    public void testPutCreateParentsSuccess() throws InterruptedException, ExecutionException {

        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE,true);
        writeTx.submit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<Top> topNode = readTx.read(LogicalDatastoreType.OPERATIONAL, TOP_PATH).get();
        assertTrue("Top node must exists after commit",topNode.isPresent());
        final Optional<TopLevelList> listNode = readTx.read(LogicalDatastoreType.OPERATIONAL, NODE_PATH).get();
        assertTrue("List node must exists after commit",listNode.isPresent());
    }

    @Test
    public void testMergeCreateParentsSuccess() throws InterruptedException, ExecutionException {

        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.merge(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE,true);
        writeTx.submit().get();

        final ReadTransaction readTx = getDataBroker().newReadOnlyTransaction();
        final Optional<Top> topNode = readTx.read(LogicalDatastoreType.OPERATIONAL, TOP_PATH).get();
        assertTrue("Top node must exists after commit",topNode.isPresent());
        final Optional<TopLevelList> listNode = readTx.read(LogicalDatastoreType.OPERATIONAL, NODE_PATH).get();
        assertTrue("List node must exists after commit",listNode.isPresent());
    }

}
