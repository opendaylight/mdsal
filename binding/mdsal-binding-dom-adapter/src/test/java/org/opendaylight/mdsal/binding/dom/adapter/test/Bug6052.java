/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.zws.test.rev160612.Wrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.zws.test.rev160612.WrapperBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.zws.test.rev160612.WrapperSecond;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.zws.test.rev160612.WrapperSecondBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.zws.test.rev160612.wrapper.second.TreeBuilder;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug6052 extends AbstractDataBrokerTest {

    @Test
    public void testBug6052() throws Exception {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        final SchemaContext schemaContext = ctx.tryToCreateSchemaContext().get();
        final BindingRuntimeContext runtimeContext = BindingRuntimeContext.create(ctx, schemaContext);
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        final BindingNormalizedNodeCodecRegistry registry =
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(runtimeContext);
        final List<String> simpleList = new ArrayList<>();
        simpleList.add("one");
        simpleList.add("two");

        final WrapperBuilder wrapperBuilder = new WrapperBuilder();
        wrapperBuilder.setSimple(simpleList);
        wrapperBuilder.setWrap("wrap");
        final Wrapper wrapper = wrapperBuilder.build();
        final NormalizedNode<?, ?> topLevelEntry = registry.toNormalizedNode(InstanceIdentifier.builder(Wrapper.class)
                .build(), wrapper).getValue();
        assertNotNull(topLevelEntry);

        final WrapperSecondBuilder wrapperBuilderSecond = new WrapperSecondBuilder();
        wrapperBuilderSecond.setSimple(simpleList);
        wrapperBuilderSecond.setWrap("wrapSecond");
        wrapperBuilderSecond.setTestLeaf1("testLeaf");

        final TreeBuilder treeB = new TreeBuilder();
        treeB.setOpen("open");
        wrapperBuilderSecond.setTestLeaf2("testLeaf");

        wrapperBuilderSecond.setTree(treeB.build());
        final WrapperSecond wrapperSecond = wrapperBuilderSecond.build();
        final NormalizedNode<?, ?> topLevelEntrySecond = registry.toNormalizedNode(InstanceIdentifier.builder
                (WrapperSecond.class).build(), wrapperSecond).getValue();
        assertNotNull(topLevelEntrySecond);

        final BindingToNormalizedNodeCodec mappingService =
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault()))));
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();
        moduleInfoBackedContext.registerModuleInfo(BindingReflections.getModuleInfo(WrapperSecond.class));
        mappingService.onGlobalContextUpdated(moduleInfoBackedContext.tryToCreateSchemaContext().get());
        final BindingCodecTree codecContext = mappingService.getCodecFactory().getCodecContext();
        final BindingCodecTreeNode<WrapperSecond> subtreeCodec = codecContext.getSubtreeCodec(InstanceIdentifier.create(WrapperSecond.class));
        final NormalizedNode<?, ?> serialized = subtreeCodec.serialize(wrapperSecond);
        assertNotNull(serialized);
    }
}