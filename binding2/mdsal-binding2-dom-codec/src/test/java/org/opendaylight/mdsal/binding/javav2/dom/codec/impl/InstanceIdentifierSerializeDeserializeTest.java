/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.ChoiceContainer;
import org.opendaylight.mdsal.gen.javav2.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.data.choice_container.identifier.simple.SimpleId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class InstanceIdentifierSerializeDeserializeTest extends AbstractBindingRuntimeTest {
    private static final InstanceIdentifier<SimpleId> BA_SIMPLE_ID = InstanceIdentifier
            .builder(ChoiceContainer.class).child(SimpleId.class).build();

    private static final QName CHOICE_CONTAINER_QNAME = ChoiceContainer.QNAME;
    private static final QName SIMPLE_ID_QNAME = SimpleId.QNAME;


    private static final YangInstanceIdentifier BI_SIMPLE_ID_PATH = YangInstanceIdentifier
        .of(CHOICE_CONTAINER_QNAME).node(SIMPLE_ID_QNAME);

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testYangIIToBindingAwareII() {
        final InstanceIdentifier<?> instanceIdentifier = registry.fromYangInstanceIdentifier(BI_SIMPLE_ID_PATH);
        assertEquals(BA_SIMPLE_ID, instanceIdentifier);
    }

    @Test
    public void testBindingAwareIIToYangIContainer() {
        final YangInstanceIdentifier yangInstanceIdentifier = registry.toYangInstanceIdentifier(BA_SIMPLE_ID);
        assertEquals(BI_SIMPLE_ID_PATH, yangInstanceIdentifier);
    }
}
