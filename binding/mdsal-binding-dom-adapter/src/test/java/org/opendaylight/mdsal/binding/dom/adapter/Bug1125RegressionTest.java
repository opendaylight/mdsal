/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * Regression test suite for Bug 1125 - Can't detect switch disconnection
 * https://bugs.opendaylight.org/show_bug.cgi?id=1125.
 */
public class Bug1125RegressionTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TopLevelList> TOP_FOO_PATH = TOP_PATH
            .child(TopLevelList.class, TOP_FOO_KEY);

    private static final InstanceIdentifier<TreeComplexUsesAugment> FOO_AUGMENT_PATH = TOP_FOO_PATH
            .augmentation(TreeComplexUsesAugment.class);

    private static final InstanceIdentifier<TreeComplexUsesAugment> WILDCARDED_AUGMENT_PATH = TOP_PATH
            .child(TopLevelList.class).augmentation(
                    TreeComplexUsesAugment.class);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return ImmutableSet.of(BindingReflections.getModuleInfo(Top.class),
                BindingReflections.getModuleInfo(TreeComplexUsesAugment.class));
    }

    @Test
    public void deleteAndListenAugment() {
        deleteAndListenAugment(TOP_PATH);

        deleteAndListenAugment(TOP_FOO_PATH);

        deleteAndListenAugment(FOO_AUGMENT_PATH);
    }

    private void deleteAndListenAugment(final InstanceIdentifier<?> path) {
        TreeComplexUsesAugment augment = writeInitialState();
        TestListener<TreeComplexUsesAugment> listener = createListener(LogicalDatastoreType.OPERATIONAL,
                WILDCARDED_AUGMENT_PATH, added(FOO_AUGMENT_PATH, augment), deleted(FOO_AUGMENT_PATH, augment));
        WriteTransaction tx = getDataBroker().newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        assertCommit(tx.commit());
        listener.verify();
    }

    private TreeComplexUsesAugment writeInitialState() {
        WriteTransaction initialTx = getDataBroker().newWriteOnlyTransaction();
        initialTx.put(LogicalDatastoreType.OPERATIONAL, TOP_PATH, new TopBuilder().build());
        TreeComplexUsesAugment fooAugment = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build())
                .build();
        initialTx.put(LogicalDatastoreType.OPERATIONAL, path(TOP_FOO_KEY), topLevelList(TOP_FOO_KEY, fooAugment));
        assertCommit(initialTx.commit());
        return fooAugment;
    }
}
