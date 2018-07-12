/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationOutputQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.Foo;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.InputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.OutputBuilder;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grpcont.Bar;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

public class ActionSerializeDeserializeTest extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier FOO_XYZZY = NodeIdentifier.create(QName.create(Foo.QNAME, "xyzzy"));

    private static final NodeIdentifier BAR_INPUT = NodeIdentifier.create(operationInputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_OUTPUT = NodeIdentifier.create(operationOutputQName(Foo.QNAME.getModule()));
    private static final NodeIdentifier BAR_XYZZY = NodeIdentifier.create(QName.create(Bar.QNAME, "xyzzy"));

    @Test
    public void testInputSerialization() {
        assertEquals(containerBuilder().withNodeIdentifier(FOO_INPUT)
            .withChild(leafBuilder().withNodeIdentifier(FOO_XYZZY).withValue("xyzzy").build())
            .build(),
            registry.toNormalizedNodeActionInput(Foo.class, new InputBuilder().setXyzzy("xyzzy").build()));
        assertEquals(containerBuilder().withNodeIdentifier(BAR_INPUT).build(),
            registry.toNormalizedNodeActionInput(Bar.class,
                new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.InputBuilder().build()));
    }

    @Test
    public void testOutputSerialization() {
        assertEquals(containerBuilder().withNodeIdentifier(FOO_OUTPUT).build(),
            registry.toNormalizedNodeActionOutput(Foo.class, new OutputBuilder().build()));
        assertEquals(containerBuilder().withNodeIdentifier(BAR_OUTPUT)
            .withChild(leafBuilder().withNodeIdentifier(BAR_XYZZY).withValue("xyzzy").build())
            .build(),
            registry.toNormalizedNodeActionOutput(Bar.class,
                new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.OutputBuilder().setXyzzy("xyzzy")
                .build()));
    }
}
