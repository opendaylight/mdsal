/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.RootCont;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.RootContBuilder;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.main.cont.grp.MainContGrp;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.main.cont.grp.MainContGrpBuilder;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.main.cont.grp.main.cont.grp.TestList1;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.main.cont.grp.main.cont.grp.TestList1Builder;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.other.grp.TestList;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.other.grp.TestListBuilder;
import org.opendaylight.yang.gen.v1.bug6113.rev160101.other.grp.TestListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class Bug6113 extends AbstractBindingRuntimeTest {

    private static final TestListKey TEST_LIST_KEY = new TestListKey("test");
    private static final InstanceIdentifier<TestList> TEST_LIST_INSTANCE_IDENTIFIER = InstanceIdentifier.builder
            (RootCont.class).child(MainContGrp.class).child(TestList.class, TEST_LIST_KEY).build();
    private static final InstanceIdentifier<RootCont> TEST_CONT_INSTANCE_IDENTIFIER = InstanceIdentifier.builder
            (RootCont.class).build();
    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void bug6113Test() {
        final TestList1 augmentLeaf = new TestList1Builder()
                .setTestLeaf("testLEaf")
                .build();

        final List<TestList> simpleTestList = new ArrayList<>();
        simpleTestList.add(new TestListBuilder()
                .setTestListLeaf("test").addAugmentation(TestList1.class, augmentLeaf)
                .build());

        final TestList testList = new TestListBuilder().setTestListLeaf("test").addAugmentation(TestList1.class, augmentLeaf)
                .build();

        final RootCont rootCont = new RootContBuilder()
                .setMainContGrp(new MainContGrpBuilder()
                        .setTestList(simpleTestList)
                        .build())
                .build();

        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom = registry.toNormalizedNode(TEST_LIST_INSTANCE_IDENTIFIER, testList);
        final Entry<InstanceIdentifier<?>, DataObject> readed = registry.fromNormalizedNode(dom.getKey(),dom.getValue());
        final TestList readedAugment = ((TestList) readed.getValue());
    }
}