/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.data.TreeLeafOnlyUsesAugment;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.dto.TreeLeafOnlyUsesAugmentBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.grp.LeafFromGroupingGrouping;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.Top;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.top.TopLevelList;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.dto.TopBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;

public class NormalizedNodeSerializeDeserializeTest extends AbstractBindingRuntimeTest {
    private BindingNormalizedNodeCodecRegistry registry;
    private static final QName TOP_LEVEL_LIST_NAME_QNAME = QName.create(TopLevelList.QNAME, "name");
    private static final QName AUGMENTED_STRING_QNAME = QName.create(TopLevelList.QNAME, "augmented-string");
    private static final QName AUGMENTED_INT_QNAME = QName.create(TopLevelList.QNAME, "augmented-int");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(LeafFromGroupingGrouping.QNAME, "simple-value");
    private static final QName SIMPLE_TYPE_QNAME = QName.create(LeafFromGroupingGrouping.QNAME, "simple-type");

    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    private static Top topBindingData() {
        final TopBuilder tb = new TopBuilder();
        tb.setTopLevelList(ImmutableList.of(new org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang
            .mdsal.test.binding.rev140701.dto.top.TopLevelListBuilder()
            .setIdentifier(new org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding
                .rev140701.key.top.top_level_list.TopLevelListKey("test"))
            .setName("test")
            .addAugmentation(TreeLeafOnlyUsesAugment.class,
                new TreeLeafOnlyUsesAugmentBuilder().setSimpleType(16).setSimpleValue("simple-value").build())
            .build()))
            .setAugmentedInt(32)
            .setAugmentedString("aug-string");
        return tb.build();
    }

    private static AugmentationIdentifier getTopLevelListAugId() {
        Set<QName> qnames = new HashSet<>();
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "simple-value"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "simple-type"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "second-simple-value"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "list-via-uses"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "container-with-uses"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "leaf-from-grouping"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "identity"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "schema-unaware-union"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "simple-type-ref"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "identity-ref"));
        qnames.add(QName.create(LeafFromGroupingGrouping.QNAME, "schema-unaware-union-ref"));

        return new AugmentationIdentifier(qnames);
    }

    private static ContainerNode topNormailziedData() {
        return ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(Top.QNAME))
            .withChild(ImmutableMapNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TopLevelList.QNAME))
                .withChild(ImmutableMapEntryNodeBuilder.create()
                    .withNodeIdentifier(new NodeIdentifierWithPredicates(TopLevelList.QNAME,
                        TOP_LEVEL_LIST_NAME_QNAME, "test"))
                    .withChild(leafNode(TOP_LEVEL_LIST_NAME_QNAME, "test"))
                    .withChild(ImmutableAugmentationNodeBuilder.create()
                        .withNodeIdentifier(getTopLevelListAugId())
                        .withChild(leafNode(SIMPLE_VALUE_QNAME, "simple-value"))
                        .withChild(leafNode(SIMPLE_TYPE_QNAME, 16))
                        .build())
                    .build())
                .build())
            .withChild(leafNode(AUGMENTED_INT_QNAME, 32))
            .withChild(leafNode(AUGMENTED_STRING_QNAME, "aug-string"))
            .build();
    }

    @Test
    public void topToNormalizedNode() {
        final Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry =
            registry.toNormalizedNode(InstanceIdentifier.create(Top.class), topBindingData());
        assertEquals(topNormailziedData(), entry.getValue());
    }

    @Test
    public void topFromNormalizedNode() {
        final Entry<InstanceIdentifier<?>, TreeNode> entry =
            registry.fromNormalizedNode(YangInstanceIdentifier.of(Top.QNAME), topNormailziedData());
        assertEquals(topBindingData(), entry.getValue());
    }

}