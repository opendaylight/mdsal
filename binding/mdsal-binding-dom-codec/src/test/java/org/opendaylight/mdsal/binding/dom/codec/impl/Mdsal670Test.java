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
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.Bar1Builder;
import org.opendaylight.yang.gen.v1.mdsal669.norev.bar.bar1.BarBuilder;
import org.opendaylight.yang.gen.v1.mdsal669.norev.foo.A1Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Mdsal670Test extends AbstractBindingCodecTest {
    private static final InstanceIdentifier<Foo> FOO_IID = InstanceIdentifier.create(Foo.class);

    @Test
    public void testLeaflistLeafref() {
        final var expected = codecContext.toNormalizedDataObject(FOO_IID,
            new FooBuilder().setA1(new A1Builder().setBar1(new Bar1Builder().setBar(new BarBuilder()
                .setBar(Set.of("bar")).build()).build()).build()).build());
        final var foo = (Foo) codecContext.fromNormalizedNode(expected.path(), expected.node()).getValue();
        assertEquals(foo.getA1().getBar1().getBar().getBar(), Set.of(5));
    }
}
