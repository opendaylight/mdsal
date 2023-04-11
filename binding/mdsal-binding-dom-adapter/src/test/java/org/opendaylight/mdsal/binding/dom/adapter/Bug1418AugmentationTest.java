/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.leafOnlyUsesAugment;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.top;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataTreeChangeListenerTest;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class Bug1418AugmentationTest extends AbstractDataTreeChangeListenerTest {
    private static final InstanceIdentifier<Top> TOP = InstanceIdentifier.create(Top.class);
    private static final InstanceIdentifier<TopLevelList> TOP_FOO = TOP.child(TopLevelList.class, TOP_FOO_KEY);
    private static final TopLevelList LIST_ELM_WITH_AUGM_LEAF =
            topLevelList(TOP_FOO_KEY, leafOnlyUsesAugment("leaf"));

    private static final InstanceIdentifier<ListViaUses> AUGMENTATION_LIST_WILDCARD =
            TOP.child(TopLevelList.class).augmentationChild(ListViaUses.class);
    private static final InstanceIdentifier<ListViaUses> USES_ONE_PATH = path(TOP_FOO_KEY, USES_ONE_KEY);
    private static final InstanceIdentifier<ListViaUses> USES_TWO_PATH = path(TOP_FOO_KEY, USES_TWO_KEY);
    private static final ListViaUses USES_ONE_VALUE = new ListViaUsesBuilder().withKey(USES_ONE_KEY).build();
    private static final ListViaUses USES_TWO_VALUE = new ListViaUsesBuilder().withKey(USES_TWO_KEY).build();

    private static final InstanceIdentifier<ContainerWithUses> AUGMENTATION_CONT_WILDCARD =
            TOP.child(TopLevelList.class).augmentationChild(ContainerWithUses.class);
    private static final InstanceIdentifier<ContainerWithUses> USES_CONT_PATH =
            TOP_FOO.augmentationChild(ContainerWithUses.class);
    private static final ContainerWithUses USES_CONT_VALUE =
            new ContainerWithUsesBuilder().setLeafFromGrouping("leaf-value").build();
    private static final ContainerWithUses USES_CONT_VALUE_UPDATED =
            new ContainerWithUsesBuilder().setLeafFromGrouping("leaf-value-updated").build();

    @Override
    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return ImmutableSet.of(BindingReflections.getModuleInfo(Top.class),
                BindingReflections.getModuleInfo(TreeComplexUsesAugment.class),
                BindingReflections.getModuleInfo(TreeLeafOnlyUsesAugment.class));
    }

    @Test
    public void leafOnlyAugmentationCreatedTest() {
        final TestListener<TopLevelList> listener =
                createListener(CONFIGURATION, TOP_FOO, added(TOP_FOO, LIST_ELM_WITH_AUGM_LEAF));

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, LIST_ELM_WITH_AUGM_LEAF);
        assertCommit(writeTx.commit());
        listener.verify();
    }

    @Test
    public void leafOnlyAugmentationUpdatedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, LIST_ELM_WITH_AUGM_LEAF);
        assertCommit(writeTx.commit());

        final var listElmWithAugmLeafUpdated = topLevelList(TOP_FOO_KEY, leafOnlyUsesAugment("leaf-updated"));
        final var listener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, LIST_ELM_WITH_AUGM_LEAF),
                replaced(TOP_FOO, LIST_ELM_WITH_AUGM_LEAF, listElmWithAugmLeafUpdated));

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP_FOO, listElmWithAugmLeafUpdated);
        assertCommit(writeTx.commit());
        listener.verify();
    }

    @Test
    public void leafOnlyAugmentationDeletedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, LIST_ELM_WITH_AUGM_LEAF);
        assertCommit(writeTx.commit());

        final var listElmWithoutAugmentation = topLevelList(TOP_FOO_KEY);
        final var listener = createListener(CONFIGURATION, TOP_FOO,
                added(TOP_FOO, LIST_ELM_WITH_AUGM_LEAF),
                replaced(TOP_FOO, LIST_ELM_WITH_AUGM_LEAF, listElmWithoutAugmentation));

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP_FOO, listElmWithoutAugmentation);
        assertCommit(writeTx.commit());
        listener.verify();
    }

    @Test
    public void complexAugmentationCreatedTest() {
        final var contListener = createListener(CONFIGURATION, AUGMENTATION_CONT_WILDCARD,
                added(USES_CONT_PATH, USES_CONT_VALUE));
        final var listListener = createListener(CONFIGURATION, AUGMENTATION_LIST_WILDCARD,
                added(USES_ONE_PATH, USES_ONE_VALUE));

        final var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY)));
        writeTx.put(CONFIGURATION, USES_CONT_PATH, USES_CONT_VALUE);
        assertCommit(writeTx.commit());

        contListener.verify();
        listListener.verify();
    }

    @Test
    public void complexAugmentationUpdatedTest() {
        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(TOP_FOO_KEY, complexUsesAugment(USES_ONE_KEY)));
        writeTx.put(CONFIGURATION, USES_CONT_PATH, USES_CONT_VALUE);
        assertCommit(writeTx.commit());

        final var contListener = createListener(CONFIGURATION, AUGMENTATION_CONT_WILDCARD,
                added(USES_CONT_PATH, USES_CONT_VALUE),
                replaced(USES_CONT_PATH, USES_CONT_VALUE, USES_CONT_VALUE_UPDATED));
        final var listListener = createListener(CONFIGURATION, AUGMENTATION_LIST_WILDCARD,
                added(USES_ONE_PATH, USES_ONE_VALUE),
                deleted(USES_ONE_PATH, USES_ONE_VALUE),
                added(USES_TWO_PATH, USES_TWO_VALUE));
        final var augmUpdated = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(USES_CONT_VALUE_UPDATED)
                .setListViaUses(Map.of(USES_TWO_KEY, USES_TWO_VALUE))
                .build();

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(TOP_FOO_KEY, augmUpdated));
        assertCommit(writeTx.commit());

        contListener.verify();
        listListener.verify();
    }

    @Test
    public void complexAugmentationDeleteTest() {
        final var augmentation = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(USES_CONT_VALUE)
                .setListViaUses(Map.of(USES_ONE_KEY, USES_ONE_VALUE))
                .build();

        var writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(CONFIGURATION, TOP, top());
        writeTx.put(CONFIGURATION, TOP_FOO, topLevelList(TOP_FOO_KEY, augmentation));
        writeTx.put(CONFIGURATION, USES_CONT_PATH, USES_CONT_VALUE);
        assertCommit(writeTx.commit());

        final var contListener = createListener(CONFIGURATION, AUGMENTATION_CONT_WILDCARD,
                added(USES_CONT_PATH, USES_CONT_VALUE),
                deleted(USES_CONT_PATH, USES_CONT_VALUE));
        final var listListener = createListener(CONFIGURATION, AUGMENTATION_LIST_WILDCARD,
                added(USES_ONE_PATH, USES_ONE_VALUE),
                deleted(USES_ONE_PATH, USES_ONE_VALUE));

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(CONFIGURATION, USES_CONT_PATH);
        writeTx.delete(CONFIGURATION, USES_ONE_PATH);
        assertCommit(writeTx.commit());

        contListener.verify();
        listListener.verify();
    }
}
