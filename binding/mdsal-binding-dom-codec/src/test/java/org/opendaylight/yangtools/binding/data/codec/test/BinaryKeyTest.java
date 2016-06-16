/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101.BinaryList;
import org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101.BinaryListBuilder;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BinaryKeyTest extends AbstractBindingRuntimeTest {
    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void BinaryKeyTest() {
        final InstanceIdentifier<BinaryList> instanceIdentifier = InstanceIdentifier.builder(BinaryList.class).build();
        final byte[] binaryKey = {1, 1, 1};
        final BinaryList binaryList = new BinaryListBuilder()
                .setBinaryItem("first")
                .setBinaryKey(binaryKey)
                .build();
        final NormalizedNode<?, ?> domTreeEntry = registry.toNormalizedNode(instanceIdentifier, binaryList)
                .getValue();
        final BinaryList deserialized = registry.deserializeFunction(instanceIdentifier)
                .apply(Optional.<NormalizedNode<?, ?>>of(domTreeEntry)).get();

        assertEquals(binaryList, deserialized);
    }
}