/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.data.DefaultPolicy;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.data.TestCont;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.dto.DefaultPolicyBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.dto.TestContBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.type.PolicyLoggingFlag;
import org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.typedef.rev170829.type.TypedefEmpty;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;


public class TypedefTest extends AbstractBindingRuntimeTest {

    private static final InstanceIdentifier<DefaultPolicy> BA_DEFAULT_POLICY =
            InstanceIdentifier.builder(DefaultPolicy.class).build();
    private static final InstanceIdentifier<TestCont> BA_TEST_CONT =
            InstanceIdentifier.builder(TestCont.class).build();

    private static final QName EMPTY_LEAF = QName.create(TestCont.QNAME, "empty-leaf");
    private static final QName EMPTY_LEAF2 = QName.create(TestCont.QNAME, "empty-leaf2");
    private static final QName EMPTY_LEAF3 = QName.create(TestCont.QNAME, "empty-leaf3");
    private static final YangInstanceIdentifier BI_TESTCONT_PATH = YangInstanceIdentifier.of(TestCont.QNAME);

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testTypedef() {
        DefaultPolicy binding = new DefaultPolicyBuilder()
                .setAction(true)
                .setAction2(new PolicyLoggingFlag(false))
                .setAction3(true)
                .build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom =
                registry.toNormalizedNode(BA_DEFAULT_POLICY, binding);
        final Entry<InstanceIdentifier<?>, TreeNode> readed =
                registry.fromNormalizedNode(dom.getKey(),dom.getValue());

        assertEquals(binding,readed.getValue());

    }

    private static TestCont getTypedefEmptyTypeBindingData() {
        return new TestContBuilder()
            .setEmptyLeaf(Empty.getInstance())
            .setEmptyLeaf2(TypedefEmpty.getDefaultInstance())
            .setEmptyLeaf3(Empty.getInstance())
            .build();
    }

    private static ContainerNode getTypedefEmptyTypeNormalizedData() {
        return ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(new NodeIdentifier(TestCont.QNAME))
            .withChild(leafNode(EMPTY_LEAF, Empty.getInstance()))
            .withChild(leafNode(EMPTY_LEAF2, Empty.getInstance()))
            .withChild(leafNode(EMPTY_LEAF3, Empty.getInstance()))
            .build();
    }

    @Test
    public void testTypedefEmptyTypeToNormalizedData() {
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom =
            registry.toNormalizedNode(BA_TEST_CONT, getTypedefEmptyTypeBindingData());

        assertEquals(getTypedefEmptyTypeNormalizedData(), dom.getValue());
    }

    @Test
    public void testTypedefEmptyTypeFromNormalizedData() {
        final Entry<InstanceIdentifier<?>, TreeNode> readed =
            registry.fromNormalizedNode(BI_TESTCONT_PATH, getTypedefEmptyTypeNormalizedData());

        assertEquals(getTypedefEmptyTypeBindingData(), readed.getValue());
    }
}
