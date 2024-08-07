/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.complexUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.leafOnlyUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Bug1418AugmentationTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TopLevelList> TOP_FOO = TOP.child(TopLevelList.class, TOP_FOO_KEY);
    private static final InstanceIdentifier<TreeLeafOnlyUsesAugment> SIMPLE_AUGMENT =
            TOP.child(TopLevelList.class, TOP_FOO_KEY).augmentation(TreeLeafOnlyUsesAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> COMPLEX_AUGMENT =
            TOP.child(TopLevelList.class, TOP_FOO_KEY).augmentation(TreeComplexUsesAugment.class);
    private static final ListViaUsesKey LIST_VIA_USES_KEY =
            new ListViaUsesKey("list key");
    private static final ListViaUsesKey LIST_VIA_USES_KEY_MOD =
            new ListViaUsesKey("list key modified");

    @Override
    protected Set<YangModuleInfo> getModuleInfos() {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(Top.class),
            BindingRuntimeHelpers.getYangModuleInfo(TreeComplexUsesAugment.class));
    }

    @Test
    public void leafOnlyAugmentationCreatedTest() {
        final var leafOnlyUsesAugment = leafOnlyUsesAugment("test leaf");
        try (var collector = createCollector(CONFIGURATION, SIMPLE_AUGMENT)) {
            collector.verifyModifications();

            final var writeTx = getDataBroker().newWriteOnlyTransaction();
            writeTx.put(CONFIGURATION, TOP, top());
            writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(new TopLevelListKey(TOP_FOO_KEY)));
            writeTx.put(CONFIGURATION, SIMPLE_AUGMENT, leafOnlyUsesAugment);
            assertCommit(writeTx.commit());

            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeLeafOnlyUsesAugment.class), leafOnlyUsesAugment));
        }
    }

    @Test
    public void leafOnlyAugmentationUpdatedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(new TopLevelListKey(TOP_FOO_KEY)));
        final var leafOnlyUsesAugmentBefore = leafOnlyUsesAugment("test leaf");
        writeTx.put(CONFIGURATION, SIMPLE_AUGMENT, leafOnlyUsesAugmentBefore);
        assertCommit(writeTx.commit());

        final var leafOnlyUsesAugmentAfter = leafOnlyUsesAugment("test leaf changed");
        try (var collector = createCollector(CONFIGURATION, SIMPLE_AUGMENT)) {
            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeLeafOnlyUsesAugment.class), leafOnlyUsesAugmentBefore));

            writeTx = getDataBroker().newWriteOnlyTransaction();
            writeTx.put(CONFIGURATION, SIMPLE_AUGMENT, leafOnlyUsesAugmentAfter);
            assertCommit(writeTx.commit());

            collector.verifyModifications(
                replaced(path(TOP_FOO_KEY, TreeLeafOnlyUsesAugment.class), leafOnlyUsesAugmentBefore,
                    leafOnlyUsesAugmentAfter));
        }
    }

    @Test
    public void leafOnlyAugmentationDeletedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(new TopLevelListKey(TOP_FOO_KEY)));
        final var leafOnlyUsesAugment = leafOnlyUsesAugment("test leaf");
        writeTx.put(CONFIGURATION, SIMPLE_AUGMENT, leafOnlyUsesAugment);
        assertCommit(writeTx.commit());

        try (var collector = createCollector(CONFIGURATION, SIMPLE_AUGMENT)) {
            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeLeafOnlyUsesAugment.class), leafOnlyUsesAugment));

            writeTx = getDataBroker().newWriteOnlyTransaction();
            writeTx.delete(CONFIGURATION, SIMPLE_AUGMENT);
            assertCommit(writeTx.commit());

            collector.verifyModifications(
                deleted(path(TOP_FOO_KEY, TreeLeafOnlyUsesAugment.class), leafOnlyUsesAugment));
        }
    }

    @Test
    public void complexAugmentationCreatedTest() {
        try (var collector = createCollector(CONFIGURATION, COMPLEX_AUGMENT)) {
            final var complexUsesAugment = complexUsesAugment(LIST_VIA_USES_KEY);
            final var writeTx = getDataBroker().newWriteOnlyTransaction();
            writeTx.put(CONFIGURATION, TOP, top());
            writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(new TopLevelListKey(TOP_FOO_KEY)));
            writeTx.put(CONFIGURATION, COMPLEX_AUGMENT, complexUsesAugment);
            assertCommit(writeTx.commit());

            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeComplexUsesAugment.class), complexUsesAugment));
        }
    }

    @Test
    public void complexAugmentationUpdatedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(new TopLevelListKey(TOP_FOO_KEY)));
        final var complexUsesAugmentBefore = complexUsesAugment(LIST_VIA_USES_KEY);
        writeTx.put(CONFIGURATION, COMPLEX_AUGMENT, complexUsesAugmentBefore);
        assertCommit(writeTx.commit());

        try (var collector = createCollector(CONFIGURATION, COMPLEX_AUGMENT)) {
            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeComplexUsesAugment.class), complexUsesAugmentBefore));

            final var complexUsesAugmentAfter = complexUsesAugment(LIST_VIA_USES_KEY_MOD);
            writeTx = getDataBroker().newWriteOnlyTransaction();
            writeTx.put(CONFIGURATION, COMPLEX_AUGMENT, complexUsesAugmentAfter);
            assertCommit(writeTx.commit());

            collector.verifyModifications(
                // While we are overwriting the augment, at the transaction ends up replacing one child with another,
                // so the Augmentation ends up not being overwritten, but modified
                subtreeModified(path(TOP_FOO_KEY, TreeComplexUsesAugment.class), complexUsesAugmentBefore,
                        complexUsesAugmentAfter));
        }
    }
}
