/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertTrue;

import javassist.ClassPool;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.ChoiceContainer;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.Top;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.choice_container.identifier.extended.ExtendedId;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.top.TopLevelList;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.key.top.top_level_list.TopLevelListKey;

public class InstanceIdentifierTest extends AbstractBindingRuntimeTest {
    private static final InstanceIdentifier<Top> BA_TOP = InstanceIdentifier.builder(Top.class).build();
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<ExtendedId> BA_EXTEND_ID = InstanceIdentifier.builder(ChoiceContainer.class)
        .child(ExtendedId.class).build();

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testInstanceIdentifierNormal() {
        assertTrue(Top.getInstanceIdentifier().equals(BA_TOP));
    }

    @Test
    public void testInstanceIdentifierOfList() {
        assertTrue(TopLevelList.getInstanceIdentifier(TOP_FOO_KEY).equals(BA_TOP_LEVEL_LIST));
    }

    @Test
    public void testInstanceIdentifierOfCase() {
        assertTrue(ExtendedId.getInstanceIdentifier().equals(BA_EXTEND_ID));
    }

}
