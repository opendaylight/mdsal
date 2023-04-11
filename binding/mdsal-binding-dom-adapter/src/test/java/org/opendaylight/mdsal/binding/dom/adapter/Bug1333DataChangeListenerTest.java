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
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * This testsuite tries to replicate bug 1333 and tests regression of it  using test-model with similar construction as
 * one reported.
 *
 * <p>
 * See  https://bugs.opendaylight.org/show_bug.cgi?id=1333 for Bug Description
 */
public class Bug1333DataChangeListenerTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<ListViaUses> AUGMENT_WILDCARD =
            TOP_PATH.child(TopLevelList.class).augmentationChild(ListViaUses.class);
    private static final InstanceIdentifier<ListViaUses> USES_ONE_PATH = path(TOP_FOO_KEY, USES_ONE_KEY);
    private static final InstanceIdentifier<ListViaUses> USES_TWO_PATH = path(TOP_FOO_KEY, USES_TWO_KEY);
    private static final ListViaUses USES_ONE_VALUE = new ListViaUsesBuilder().withKey(USES_ONE_KEY).build();
    private static final ListViaUses USES_TWO_VALUE = new ListViaUsesBuilder().withKey(USES_TWO_KEY).build();

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return ImmutableSet.of(BindingReflections.getModuleInfo(Top.class),
                BindingReflections.getModuleInfo(TreeComplexUsesAugment.class));
    }

    private static Top topWithListItem() {
        return top(topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY)));
    }

    private Top writeTopWithListItem(final LogicalDatastoreType store) {
        ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        Top topItem = topWithListItem();
        tx.put(store, TOP_PATH, topItem);
        assertCommit(tx.commit());
        return topItem;
    }

    private void deleteItem(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        tx.delete(store, path);
        assertCommit(tx.commit());
    }

    @Test
    public void writeTopWithListItemAugmentedListenTopSubtree() {
        TestListener<Top> listener = createListener(CONFIGURATION, TOP_PATH, added(TOP_PATH, topWithListItem()));
        writeTopWithListItem(CONFIGURATION);
        listener.verify();
    }

    @Test
    public void writeTopWithListItemAugmentedListenAugmentSubtreeWildcarded() {
        final var listener = createListener(CONFIGURATION, AUGMENT_WILDCARD,
                added(USES_ONE_PATH, USES_ONE_VALUE), added(USES_TWO_PATH, USES_TWO_VALUE));
        writeTopWithListItem(CONFIGURATION);
        listener.verify();
    }

    @Test
    public void deleteAugmentChildListenTopSubtree() {
        final var top = writeTopWithListItem(CONFIGURATION);
        final var listener = createListener(CONFIGURATION, TOP_PATH,
                added(TOP_PATH, top),
                subtreeModified(TOP_PATH, top, top(topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_TWO_KEY)))));
        deleteItem(CONFIGURATION, USES_ONE_PATH);
        listener.verify();
    }

    @Test
    public void deleteAugmentChildListenAugmentSubtreeWildcarded() {
        writeTopWithListItem(CONFIGURATION);
        final var listener = createListener(CONFIGURATION, AUGMENT_WILDCARD,
                added(USES_ONE_PATH, USES_ONE_VALUE),
                added(USES_TWO_PATH, USES_TWO_VALUE),
                deleted(USES_ONE_PATH, USES_ONE_VALUE));
        deleteItem(CONFIGURATION, USES_ONE_PATH);
        listener.verify();
    }
}
