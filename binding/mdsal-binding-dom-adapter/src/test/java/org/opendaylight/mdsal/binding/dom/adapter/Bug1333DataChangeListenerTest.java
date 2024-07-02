/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.USES_ONE_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.USES_TWO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.complexUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * This testsuite tries to replicate bug 1333 and tests regresion of it  using test-model with similar construction as
 * one reported.
 *
 * <p>
 * See  https://bugs.opendaylight.org/show_bug.cgi?id=1333 for Bug Description
 */
public class Bug1333DataChangeListenerTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> AUGMENT_WILDCARD =
            TOP_PATH.child(TopLevelList.class).augmentation(TreeComplexUsesAugment.class);

    @Override
    protected Set<YangModuleInfo> getModuleInfos() {
        return Set.of(BindingRuntimeHelpers.getYangModuleInfo(Top.class),
            BindingRuntimeHelpers.getYangModuleInfo(TreeComplexUsesAugment.class));
    }

    private static Top topWithListItem() {
        return top(topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY)));
    }

    public Top writeTopWithListItem(final LogicalDatastoreType store) {
        var tx = getDataBroker().newWriteOnlyTransaction();
        var topItem = topWithListItem();
        tx.put(store, TOP_PATH, topItem);
        assertCommit(tx.commit());
        return topItem;
    }

    public void deleteItem(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        var tx = getDataBroker().newWriteOnlyTransaction();
        tx.delete(store, path);
        assertCommit(tx.commit());
    }

    @Test
    public void writeTopWithListItemAugmentedListenTopSubtree() {
        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, TOP_PATH)) {
            collector.verifyModifications();

            writeTopWithListItem(LogicalDatastoreType.CONFIGURATION);

            collector.verifyModifications(added(TOP_PATH, topWithListItem()));
        }
    }

    @Test
    public void writeTopWithListItemAugmentedListenAugmentSubtreeWildcarded() {
        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, AUGMENT_WILDCARD)) {
            collector.verifyModifications();

            writeTopWithListItem(LogicalDatastoreType.CONFIGURATION);

            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeComplexUsesAugment.class), complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY)));
        }
    }

    @Test
    public void deleteAugmentChildListenTopSubtree() {
        final var top = writeTopWithListItem(LogicalDatastoreType.CONFIGURATION);

        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, TOP_PATH)) {
            collector.verifyModifications(added(TOP_PATH, top));

            deleteItem(LogicalDatastoreType.CONFIGURATION, path(TOP_FOO_KEY, USES_ONE_KEY));

            collector.verifyModifications(
                subtreeModified(TOP_PATH, top, top(topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_TWO_KEY)))));
        }
    }

    @Test
    public void deleteAugmentChildListenAugmentSubtreeWildcarded() {
        writeTopWithListItem(LogicalDatastoreType.CONFIGURATION);

        try (var collector = createCollector(LogicalDatastoreType.CONFIGURATION, AUGMENT_WILDCARD)) {
            collector.verifyModifications(
                added(path(TOP_FOO_KEY, TreeComplexUsesAugment.class), complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY)));

            deleteItem(LogicalDatastoreType.CONFIGURATION, path(TOP_FOO_KEY, USES_ONE_KEY));

            collector.verifyModifications(
                subtreeModified(path(TOP_FOO_KEY, TreeComplexUsesAugment.class),
                    complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY), complexUsesAugment(USES_TWO_KEY)));
        }
    }
}
