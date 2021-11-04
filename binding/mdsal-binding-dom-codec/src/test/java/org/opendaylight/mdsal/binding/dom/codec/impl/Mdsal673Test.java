/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

public class Mdsal673Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);

    @Test
    public void testNonnullContainer() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.create(FOO),
            Builders.containerBuilder().withNodeIdentifier(FOO).build());
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var obj = entry.getValue();
        assertThat(obj, instanceOf(Foo.class));
        assertEquals(new BarBuilder().build(), ((Foo) obj).nonnullBar());
    }
}
