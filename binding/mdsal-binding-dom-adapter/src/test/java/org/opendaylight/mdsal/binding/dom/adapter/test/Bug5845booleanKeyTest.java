/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListIntKey;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101._boolean.container.BooleanListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class Bug5845booleanKeyTest extends AbstractDataBrokerTest {

    @Test
    public void testBug5845() throws Exception {
        final BindingToNormalizedNodeCodec mappingService = new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault()))));
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();
        moduleInfoBackedContext.registerModuleInfo(BindingReflections.getModuleInfo(BooleanContainer.class));
        mappingService.onGlobalContextUpdated(moduleInfoBackedContext.tryToCreateSchemaContext().get());

        final BooleanContainer booleanContainer = new BooleanContainerBuilder().setBooleanList(Collections
                .singletonList(new BooleanListBuilder()
                        .setKey(new BooleanListKey(true, true))
                        .setBooleanLeaf1(true)
                        .setBooleanLeaf2(true)
                        .build()))
                .build();

        final BooleanContainer booleanContainerInt = new BooleanContainerBuilder().setBooleanListInt(Collections
                .singletonList(new BooleanListIntBuilder()
                        .setKey(new BooleanListIntKey((byte) 1))
                        .setBooleanLeafInt((byte) 1)
                        .build()))
                .build();

        final BindingCodecTree codecContext = mappingService.getCodecFactory().getCodecContext();
        final BindingCodecTreeNode<BooleanContainer> subtreeCodec = codecContext.getSubtreeCodec(InstanceIdentifier.create(BooleanContainer.class));
        final NormalizedNode<?, ?> serializedInt = subtreeCodec.serialize(booleanContainerInt);
        assertNotNull(serializedInt);
        final NormalizedNode<?, ?> serialized = subtreeCodec.serialize(booleanContainer);
        assertNotNull(serialized);
    }
}