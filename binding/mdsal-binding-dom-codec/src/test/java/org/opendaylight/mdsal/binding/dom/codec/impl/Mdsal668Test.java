/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.Bar;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

public class Mdsal668Test extends AbstractBindingCodecTest {
    @Test
    public void testLeaflistLeafref() {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(Foo.QNAME))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                .withChild(Builders.leafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                    .build())
                .build())
            .build(),
            codecContext.toNormalizedNode(InstanceIdentifier.create(Foo.class),
                new FooBuilder().setBar(new BarBuilder().setBar(List.of()).build()).build()).getValue());
    }
}
