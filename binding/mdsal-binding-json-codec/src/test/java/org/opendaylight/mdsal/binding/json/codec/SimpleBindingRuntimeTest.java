/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.json.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Id;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class SimpleBindingRuntimeTest extends AbstractBindingRuntimeTest {
    private static BindingNormalizedNodeSerializer bindingCodecContext = new BindingCodecContext(getRuntimeContext());

    @Test
    public void testSimpleContainer() {
        final var cont = new ContBuilder().setVlanId(new VlanId(new Id(Uint16.valueOf(30)))).build();
        final var normalizedNode = bindingCodecContext.toNormalizedNode(InstanceIdentifier.create(Cont.class), cont)
            .getValue();
        assertThat(normalizedNode, instanceOf(ContainerNode.class));
    }
}
