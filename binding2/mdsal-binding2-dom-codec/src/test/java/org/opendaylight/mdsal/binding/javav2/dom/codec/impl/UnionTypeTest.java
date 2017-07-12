/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.gen.javav2.bug5446.rev170711.data.Root;
import org.opendaylight.mdsal.gen.javav2.bug5446.rev170711.dto.RootBuilder;
import org.opendaylight.mdsal.gen.javav2.bug5446.rev170711.type.IpAddressBinary;
import org.opendaylight.mdsal.gen.javav2.bug5446.rev170711.type.IpAddressBinaryBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.union.rev170711.data.Wrapper;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.union.rev170711.dto.WrapperBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.union.rev170711.type.TopLevel;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.union.rev170711.type.TopLevelBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

public class UnionTypeTest extends AbstractBindingRuntimeTest {

    private static final String testString = "testtesttest";

    private static final QName WRAPPER_QNAME = QName.create
            ("urn:opendaylight:params:xml:ns:yang:mdsal:test:union", "2017-07-11", "wrapper");
    private static final QName WRAP_LEAF_QNAME = QName.create(WRAPPER_QNAME, "wrap");

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void unionTest() {
        TopLevel topLevel = TopLevelBuilder.getDefaultInstance(testString);
        Wrapper wrapper = new WrapperBuilder().setWrap(topLevel).build();
        NormalizedNode<?, ?> topLevelEntry = registry.toNormalizedNode(InstanceIdentifier.builder(Wrapper.class).build(), wrapper).getValue();

        ContainerNode containerNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(WRAPPER_QNAME))
                .withChild(ImmutableNodes.leafNode(WRAP_LEAF_QNAME, testString))
                .build();
        assertEquals(topLevelEntry, containerNode);
    }

    @Ignore
    @Test
    public void bug5446Test() {
        IpAddressBinary ipAddress = IpAddressBinaryBuilder.getDefaultInstance("fwAAAQ==");
        Root root = new RootBuilder().setIpAddress(ipAddress).build();
        NormalizedNode<?, ?> rootNode = registry.toNormalizedNode(InstanceIdentifier.builder(Root.class).build(), root)
                .getValue();

        Entry<InstanceIdentifier<?>, TreeNode> rootEntry = registry.fromNormalizedNode(
                YangInstanceIdentifier.of(rootNode.getNodeType()), rootNode);

        TreeNode rootObj = rootEntry.getValue();
        assertTrue(rootObj instanceof Root);
        IpAddressBinary desIpAddress = ((Root) rootObj).getIpAddress();
        assertEquals(ipAddress, desIpAddress);
    }
}
