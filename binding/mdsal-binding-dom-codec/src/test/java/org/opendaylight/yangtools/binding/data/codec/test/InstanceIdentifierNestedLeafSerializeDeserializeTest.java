/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug8237.rev180405.simple.container.SimpleList;
import org.opendaylight.yang.gen.v1.bug8237.rev180405.simple.container.simple.list.ContUnderList;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class InstanceIdentifierNestedLeafSerializeDeserializeTest extends AbstractBindingRuntimeTest {

    private static final String SIMPLE_LIST_KEY_NAME_VALUE = "simple-key-list-name";

    private static final QName SIMPLE_CONTAINER_QNAME = QName.create("bug8237", "2017-16-05", "simple-container");
    private static final QName SIMPLE_LIST_QNAME = QName.create(SIMPLE_CONTAINER_QNAME, "simple-list");
    private static final QName SIMPLE_LIST_KEY_NAME_QNAME = QName.create(SIMPLE_CONTAINER_QNAME, "name");
    private static final QName CONT_UNDER_LIST_QNAME = QName.create(SIMPLE_CONTAINER_QNAME, "cont-under-list");
    private static final QName NESTED_NAME_QNAME = QName.create(SIMPLE_CONTAINER_QNAME, "nested-name");

    private static final YangInstanceIdentifier BI_SIMPLE_CONTAINER_PATH =
            YangInstanceIdentifier.builder().node(SIMPLE_CONTAINER_QNAME).build();
    private static final YangInstanceIdentifier BI_SIMPLE_LIST_PATH = BI_SIMPLE_CONTAINER_PATH.node(SIMPLE_LIST_QNAME);
    private static final YangInstanceIdentifier BI_SIMPLE_LIST_KEY_PATH =
            BI_SIMPLE_LIST_PATH.node(new YangInstanceIdentifier.NodeIdentifierWithPredicates(SIMPLE_LIST_QNAME,
                    SIMPLE_LIST_KEY_NAME_QNAME, SIMPLE_LIST_KEY_NAME_VALUE));
    private static final YangInstanceIdentifier BI_CONT_UNDER_LIST_PATH =
            BI_SIMPLE_LIST_KEY_PATH.node(CONT_UNDER_LIST_QNAME);
    private static final YangInstanceIdentifier BI_NESTED_NAME_PATH =
            BI_CONT_UNDER_LIST_PATH.node(new YangInstanceIdentifier.NodeIdentifier(NESTED_NAME_QNAME));

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
    public void leafReferenceTest() {
        final InstanceIdentifier<?> fromYangInstanceIdentifier =
                this.registry.fromYangInstanceIdentifier(BI_NESTED_NAME_PATH);
        assertNull(fromYangInstanceIdentifier);
    }

    @Test
    public void containerReferenceTest() {
        final InstanceIdentifier<?> fromYangInstanceIdentifier =
                this.registry.fromYangInstanceIdentifier(BI_CONT_UNDER_LIST_PATH);
        assertNotNull(fromYangInstanceIdentifier);
        assertEquals(ContUnderList.class, fromYangInstanceIdentifier.getTargetType());
    }

    @Test
    public void listReferenceTest() {
        final InstanceIdentifier<?> fromYangInstanceIdentifier =
                this.registry.fromYangInstanceIdentifier(BI_SIMPLE_LIST_KEY_PATH);
        assertNotNull(fromYangInstanceIdentifier);
        assertEquals(SimpleList.class, fromYangInstanceIdentifier.getTargetType());
    }
}
