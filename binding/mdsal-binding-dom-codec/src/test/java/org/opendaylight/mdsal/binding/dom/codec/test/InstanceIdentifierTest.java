/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.aug.norev.cont.cont.choice.ContAug;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.Cont;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.ContChoice;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.cont.cont.choice.ContBase;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal45.base.norev.grp.GrpCont;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class InstanceIdentifierTest extends AbstractBindingRuntimeTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
            .augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST
            .augmentation(TreeComplexUsesAugment.class);
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testComplexAugmentationSerialization() {
        final YangInstanceIdentifier yangII = registry.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES);
        final PathArgument lastArg = yangII.getLastPathArgument();
        assertTrue("Last argument should be AugmentationIdentifier", lastArg instanceof AugmentationIdentifier);
        final InstanceIdentifier<?> bindingII = registry.fromYangInstanceIdentifier(yangII);
        assertEquals(BA_TREE_COMPLEX_USES, bindingII);
    }

    @Test
    public void testLeafOnlyAugmentationSerialization() {
        final PathArgument leafOnlyLastArg = registry.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY).getLastPathArgument();
        assertTrue("Last argument should be AugmentationIdentifier", leafOnlyLastArg instanceof AugmentationIdentifier);
        assertTrue(((AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
    }

    @Test
    public void testChoiceCaseGroupingAddressing() {
        final YangInstanceIdentifier contBase = registry.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContBase.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME), NodeIdentifier.create(GrpCont.QNAME)), contBase);

        final YangInstanceIdentifier contAug = registry.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child(ContAug.class, GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.withModule(ContAug.QNAME.getModule()))), contAug);

        // Legacy: downcast the child to Class, losing type safety but still working. Faced with ambiguity, it will
        //         select the lexically-lower class
        assertEquals(1, ContBase.class.getCanonicalName().compareTo(ContAug.class.getCanonicalName()));
        final YangInstanceIdentifier contAugLegacy = registry.toYangInstanceIdentifier(
            InstanceIdentifier.builder(Cont.class).child((Class) GrpCont.class).build());
        assertEquals(YangInstanceIdentifier.create(NodeIdentifier.create(Cont.QNAME),
            NodeIdentifier.create(ContChoice.QNAME),
            NodeIdentifier.create(GrpCont.QNAME.withModule(ContAug.QNAME.getModule()))), contAugLegacy);

        // FIXME: root choice handling is busted
        //      final YangInstanceIdentifier rootAugLegacy = registry.toYangInstanceIdentifier(
        //          InstanceIdentifier.create((Class) GrpCont.class));
    }
}
