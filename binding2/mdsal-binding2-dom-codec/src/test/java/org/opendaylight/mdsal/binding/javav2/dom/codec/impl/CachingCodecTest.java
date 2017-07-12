/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev170711.data.Top;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev170711.data.top.TopLevelList;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev170711.dto.TopBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev170711.dto.top.TopLevelListBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev170711.key.top.top_level_list.TopLevelListKey;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class CachingCodecTest extends AbstractBindingRuntimeTest {

    private static final NodeIdentifier TOP_LEVEL_LIST_ARG = new NodeIdentifier(TopLevelList.QNAME);
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final List<TopLevelList> TWO_LIST = createList(2);
    private static final List<TopLevelList> THREE_LIST = createList(3);

    private static final Top TOP_TWO_LIST_DATA = new TopBuilder().setTopLevelList(TWO_LIST).build();
    private static final Top TOP_THREE_LIST_DATA = new TopBuilder().setTopLevelList(THREE_LIST).build();

    private BindingNormalizedNodeCodecRegistry registry;
    private BindingTreeNodeCodec<Top> topNode;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
        topNode = registry.getCodecContext().getSubtreeCodec(TOP_PATH);

    }


    private static List<TopLevelList> createList(final int num) {

        final ImmutableList.Builder<TopLevelList> builder = ImmutableList.builder();
        for (int i = 0; i < num; i++) {
            final TopLevelListKey key = new TopLevelListKey("test-" + i);
            builder.add(new TopLevelListBuilder().setKey(key).build());
        }
        return builder.build();
    }

    @Test
    public void testListCache() {
        final BindingNormalizedNodeCachingCodec<Top> cachingCodec = createCachingCodec(TopLevelList.class);
        final NormalizedNode<?, ?> first = cachingCodec.serialize(TOP_TWO_LIST_DATA);
        final NormalizedNode<?, ?> second = cachingCodec.serialize(TOP_TWO_LIST_DATA);

        assertNotSame(first, second);
        assertEquals(first, second);
        verifyListItemSame(first, second);

        final NormalizedNode<?, ?> third = cachingCodec.serialize(TOP_THREE_LIST_DATA);
        verifyListItemSame(first, third);
        verifyListItemSame(second, third);
    }


    @Test
    public void testTopAndListCache() {
        final BindingNormalizedNodeCachingCodec<Top> cachingCodec = createCachingCodec(Top.class, TopLevelList.class);
        final NormalizedNode<?, ?> first = cachingCodec.serialize(TOP_TWO_LIST_DATA);
        final NormalizedNode<?, ?> second = cachingCodec.serialize(TOP_TWO_LIST_DATA);

        assertEquals(first, second);
        assertSame(first, second);

        final NormalizedNode<?, ?> third = cachingCodec.serialize(TOP_THREE_LIST_DATA);
        verifyListItemSame(first, third);
    }

    @SafeVarargs
    private final BindingNormalizedNodeCachingCodec<Top> createCachingCodec(
            final Class<? extends TreeNode>... classes) {
        return topNode.createCachingCodec(ImmutableSet.<Class<? extends TreeNode>>copyOf(classes));
    }

    private static void verifyListItemSame(final NormalizedNode<?, ?> firstTop, final NormalizedNode<?, ?> secondTop) {
        final Collection<MapEntryNode> initialNodes = getListItems(firstTop).getValue();
        final MapNode secondMap = getListItems(secondTop);

        for (final MapEntryNode initial : initialNodes) {
            final MapEntryNode second = secondMap.getChild(initial.getIdentifier()).get();
            assertEquals(initial, second);
            assertSame(initial, second);
        }
    }


    private static MapNode getListItems(final NormalizedNode<?, ?> top) {
        return ((MapNode) ((DataContainerNode<?>) top).getChild(TOP_LEVEL_LIST_ARG).get());
    }

}
