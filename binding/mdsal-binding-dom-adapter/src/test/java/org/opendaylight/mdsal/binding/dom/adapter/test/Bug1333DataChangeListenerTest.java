/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.opendaylight.mdsal.binding.dom.adapter.test.AssertCollections.assertContains;
import static org.opendaylight.mdsal.binding.dom.adapter.test.AssertCollections.assertEmpty;
import static org.opendaylight.mdsal.binding.dom.adapter.test.AssertCollections.assertNotContains;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.USES_ONE_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.USES_TWO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.complexUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import org.opendaylight.mdsal.common.api.AsyncDataChangeEvent;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.AsyncDataBroker.DataChangeScope;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 * This testsuite tries to replicate bug 1333 and tests regresion of it
 * using test-model with similar construction as one reported.
 *
 *
 * See  https://bugs.opendaylight.org/show_bug.cgi?id=1333 for Bug Description
 *
 */
public class Bug1333DataChangeListenerTest extends AbstractDataChangeListenerTest{

    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);

    private static final InstanceIdentifier<?> AUGMENT_WILDCARD = TOP_PATH.child(TopLevelList.class).augmentation(
            TreeComplexUsesAugment.class);

    public void writeTopWithListItem(final LogicalDatastoreType store) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final Top topItem = top(topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY, USES_TWO_KEY)));
        tx.put(store, TOP_PATH, topItem);
        assertCommit(tx.submit());
    }

    public void deleteItem(final LogicalDatastoreType store, final InstanceIdentifier<?> path) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        tx.delete(store, path);
        assertCommit(tx.submit());
    }

    @Test
    public void writeTopWithListItemAugmentedListenTopSubtree() {
        final TestListener listener = createListener(CONFIGURATION,TOP_PATH, DataChangeScope.SUBTREE);
        listener.startCapture();

        writeTopWithListItem(CONFIGURATION);

        final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event = listener.event();

        assertContains(event.getCreatedData(), TOP_PATH);
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY));
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, TreeComplexUsesAugment.class));
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, USES_ONE_KEY));
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, USES_TWO_KEY));

        assertEmpty(event.getUpdatedData());
        assertEmpty(event.getRemovedPaths());
    }

    @Test
    public void writeTopWithListItemAugmentedListenAugmentSubtreeWildcarded() {
        final TestListener listener = createListener(CONFIGURATION,AUGMENT_WILDCARD, DataChangeScope.SUBTREE);
        listener.startCapture();
        writeTopWithListItem(CONFIGURATION);

        final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event = listener.event();

        /*
         * Event should not contain parent nodes
         */
        assertNotContains(event.getCreatedData(), TOP_PATH, path(TOP_FOO_KEY));

        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, TreeComplexUsesAugment.class));
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, USES_ONE_KEY));
        assertContains(event.getCreatedData(), path(TOP_FOO_KEY, USES_TWO_KEY));

        assertEmpty(event.getUpdatedData());
        assertEmpty(event.getRemovedPaths());
    }

    @Test
    public void deleteAugmentChildListenTopSubtree() {
        writeTopWithListItem(CONFIGURATION);
        final TestListener listener = createListener(CONFIGURATION, TOP_PATH, DataChangeScope.SUBTREE);
        final InstanceIdentifier<?> deletePath = path(TOP_FOO_KEY,USES_ONE_KEY);
        deleteItem(CONFIGURATION,deletePath);

        final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event = listener.event();


        assertEmpty(event.getCreatedData());

        assertContains(event.getRemovedPaths(), deletePath);

        assertContains(event.getUpdatedData(), TOP_PATH);
        assertContains(event.getUpdatedData(), path(TOP_FOO_KEY));
        assertContains(event.getUpdatedData(), path(TOP_FOO_KEY, TreeComplexUsesAugment.class));

        assertNotContains(event.getCreatedData(), path(TOP_FOO_KEY, USES_TWO_KEY));
    }

    @Test
    public void deleteAugmentChildListenAugmentSubtreeWildcarded() {
        writeTopWithListItem(CONFIGURATION);

        final TestListener listener = createListener(CONFIGURATION, AUGMENT_WILDCARD, DataChangeScope.SUBTREE);
        final InstanceIdentifier<?> deletePath = path(TOP_FOO_KEY,USES_ONE_KEY);
        deleteItem(CONFIGURATION,deletePath);
        final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event = listener.event();

        assertEmpty(event.getCreatedData());

        assertContains(event.getUpdatedData(), path(TOP_FOO_KEY, TreeComplexUsesAugment.class));

        /*
         * Event should not contain parent nodes
         */
        assertNotContains(event.getUpdatedData(), TOP_PATH, path(TOP_FOO_KEY));

        assertContains(event.getRemovedPaths(), deletePath);
    }

}
