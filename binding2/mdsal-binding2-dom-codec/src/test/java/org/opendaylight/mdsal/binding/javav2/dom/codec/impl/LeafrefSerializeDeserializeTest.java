/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data.Cont;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data.Cont.Ref;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data.ContInt32;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.data.ContInt32.RefUnionInt32;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.dto.ContBuilder;
import org.opendaylight.mdsal.gen.javav2.bug8449.rev170516.dto.ContInt32Builder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class LeafrefSerializeDeserializeTest extends AbstractBindingRuntimeTest {

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        this.registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        this.registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void listReferenceTest() {
        final YangInstanceIdentifier contYII = YangInstanceIdentifier.builder().node(Cont.QNAME).build();
        final InstanceIdentifier<?> fromYangInstanceIdentifier = this.registry.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final InstanceIdentifier<Cont> BA_II_CONT = InstanceIdentifier.builder(Cont.class).build();
        final Ref refVal = new Ref("myvalue");
        final Cont data = new ContBuilder().setRef(refVal).build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode =
                this.registry.toNormalizedNode(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final Entry<InstanceIdentifier<?>, TreeNode> fromNormalizedNode =
                this.registry.fromNormalizedNode(contYII, normalizedNode.getValue());
        assertNotNull(fromNormalizedNode);
        final Cont value = (Cont) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRef());
    }

    @Test
    public void uint32LeafrefTest() {
        final YangInstanceIdentifier contYII = YangInstanceIdentifier.builder().node(ContInt32.QNAME).build();
        final InstanceIdentifier<?> fromYangInstanceIdentifier = this.registry.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final InstanceIdentifier<ContInt32> BA_II_CONT = InstanceIdentifier.builder(ContInt32.class).build();
        final RefUnionInt32 refVal = new RefUnionInt32(Uint32.valueOf(5L));
        final ContInt32 data = new ContInt32Builder().setRefUnionInt32(refVal).build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode =
                this.registry.toNormalizedNode(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final Entry<InstanceIdentifier<?>, TreeNode> fromNormalizedNode =
                this.registry.fromNormalizedNode(contYII, normalizedNode.getValue());
        assertNotNull(fromNormalizedNode);
        final ContInt32 value = (ContInt32) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRefUnionInt32());
    }
}

