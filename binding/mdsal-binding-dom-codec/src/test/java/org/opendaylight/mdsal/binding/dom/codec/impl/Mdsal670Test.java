/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal669.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal669.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.Bar1;
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.Bar1Builder;
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.bar1.Bar;
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.bar1.BarBuilder;
import org.opendaylight.yang.gen.v1.mdsal669.norev.foo.A1Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class Mdsal670Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);
    private static final InstanceIdentifier<Foo> FOO_IID = InstanceIdentifier.create(Foo.class);

    @Test
    public void testLeaflistLeafref() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(FOO)
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(Bar1.QNAME))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                        // FIXME: MDSAL-670: these should get translated to YangInstanceIdentifier.of(FOO)
                        .withChild(ImmutableNodes.leafSetEntry(Bar.QNAME, YangInstanceIdentifier.of(FOO)))
                        .build())
                    .build())
                .build())
            .build(),
            codecContext.toNormalizedDataObject(FOO_IID,
                new FooBuilder().setA1(new A1Builder().setBar1(new Bar1Builder().setBar(new BarBuilder()
                        .setBar(Set.of(FOO_IID)).build()).build()).build()).build()).node());
    }
}
