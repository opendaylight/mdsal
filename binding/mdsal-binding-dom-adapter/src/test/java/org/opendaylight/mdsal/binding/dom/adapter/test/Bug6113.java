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

public class Bug6113 extends AbstractDataBrokerTest {

    @Test
    public void testBug6113() throws Exception {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        final SchemaContext schemaContext = ctx.tryToCreateSchemaContext().get();
        final BindingRuntimeContext runtimeContext = BindingRuntimeContext.create(ctx, schemaContext);
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        final BindingNormalizedNodeCodecRegistry registry =
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(runtimeContext);


//        final TestList1 testList1 = (new TestList1Builder()
//            .setTestContainer(new TestContainerBuilder().setTestLeaf("test").build())
//            .build());
//
//        final List<TestList> simpleTestList = new ArrayList<>();
//        simpleTestList.add(new TestListBuilder()
//            .setTestListLeaf("test")
//            .addAugmentation(TestList1.class, testList1)
//            .build());
//
//        final MainCont mainCont = new MainContBuilder()
//                .setTestList(simpleTestList)
//                .build();




//        final WrapperBuilder wrapperBuilder = new WrapperBuilder();
//        wrapperBuilder.setSimple(simpleList);
//        wrapperBuilder.setWrap("wrap");
//        final Wrapper wrapper = wrapperBuilder.build();
//        final NormalizedNode<?, ?> topLevelEntry = registry.toNormalizedNode(InstanceIdentifier.builder(Wrapper.class)
//                .build(), wrapper).getValue();
//        assertNotNull(topLevelEntry);
//
//        final WrapperSecondBuilder wrapperBuilderSecond = new WrapperSecondBuilder();
//        wrapperBuilderSecond.setSimple(simpleList);
//        wrapperBuilderSecond.setWrap("wrapSecond");
//        wrapperBuilderSecond.setTestLeaf1("testLeaf");
//
//        final TreeBuilder treeB = new TreeBuilder();
//        treeB.setOpen("open");
//        wrapperBuilderSecond.setTestLeaf2("testLeaf");
//
//        wrapperBuilderSecond.setTree(treeB.build());
//        final WrapperSecond wrapperSecond = wrapperBuilderSecond.build();
//        final NormalizedNode<?, ?> topLevelEntrySecond = registry.toNormalizedNode(InstanceIdentifier.builder
//                (WrapperSecond.class).build(), wrapperSecond).getValue();
//        assertNotNull(topLevelEntrySecond);
//
//        final BindingToNormalizedNodeCodec mappingService =
//                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
//                        new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault()))));
//        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();
//        moduleInfoBackedContext.registerModuleInfo(BindingReflections.getModuleInfo(MainCont.class));
//        mappingService.onGlobalContextUpdated(moduleInfoBackedContext.tryToCreateSchemaContext().get());
//        final BindingCodecTree codecContext = mappingService.getCodecFactory().getCodecContext();
//        final BindingCodecTreeNode<MainCont> subtreeCodec = codecContext.getSubtreeCodec(InstanceIdentifier.create(MainCont.class));
//        final NormalizedNode<?, ?> serialized = subtreeCodec.serialize(mainCont);
//        assertNotNull(serialized);
    }
}