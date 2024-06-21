/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class WriteTransactionTest extends AbstractDataBrokerTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final TopLevelListKey TOP_LIST_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> NODE_PATH = TOP_PATH.child(TopLevelList.class, TOP_LIST_KEY);
    private static final InstanceIdentifier<TreeLeafOnlyUsesAugment> NODE_AUGMENT_PATH =
        NODE_PATH.augmentation(TreeLeafOnlyUsesAugment.class);
    private static final TopLevelList NODE = new TopLevelListBuilder().withKey(TOP_LIST_KEY).build();
    private static final TreeLeafOnlyUsesAugment NODE_AUGMENT = new TreeLeafOnlyUsesAugmentBuilder()
        .setLeafFromGrouping("foo")
        .build();

    @Test
    public void test() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, TOP_PATH, new TopBuilder().build());
        writeTx.put(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE);
        writeTx.commit().get();
    }

    @Test
    public void testPutCreateParentsSuccess() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE);
        writeTx.commit().get();

        assertTop(new TopBuilder().setTopLevelList(BindingMap.of(NODE)).build());
    }

    @Test
    public void testPutCreateAugmentationParentsSuccess() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, NODE_AUGMENT_PATH, NODE_AUGMENT);
        writeTx.commit().get();

        assertTop(new TopBuilder()
            .setTopLevelList(BindingMap.of(new TopLevelListBuilder()
                .withKey(TOP_LIST_KEY)
                .addAugmentation(NODE_AUGMENT)
                .build()))
            .build());
    }

    @Test
    public void testPutCreateParentsSuperfluous() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, TOP_PATH, new TopBuilder().build());
        writeTx.commit().get();
    }

    @Test
    public void testMergeCreateParentsSuccess() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, NODE_PATH, NODE);
        writeTx.commit().get();

        assertTop(new TopBuilder().setTopLevelList(BindingMap.of(NODE)).build());
    }

    @Test
    public void testMergeCreateAugmentationParentsSuccess() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, NODE_AUGMENT_PATH, NODE_AUGMENT);
        writeTx.commit().get();

        assertTop(new TopBuilder()
            .setTopLevelList(BindingMap.of(new TopLevelListBuilder()
                .withKey(TOP_LIST_KEY)
                .addAugmentation(NODE_AUGMENT)
                .build()))
            .build());
    }

    @Test
    public void testMergeCreateParentsSuperfluous() throws Exception {
        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, TOP_PATH, new TopBuilder().build());
        writeTx.commit().get();
    }

    private void assertTop(final Top expected) throws Exception {
        try (var readTx = getDataBroker().newReadOnlyTransaction()) {
            assertEquals(Optional.of(expected), readTx.read(LogicalDatastoreType.OPERATIONAL, TOP_PATH).get());
        }
    }
}
