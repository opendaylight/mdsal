/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.odl.actions.norev.cont.foo.InputBuilder;

public class ActionSerializeDeserializeTest extends AbstractBindingCodecTest {

    @Test
    public void testInputSerialization() {
        registry.toNormalizedNodeActionInput(new InputBuilder().setXyzzy("xyzzy").build());
        registry.toNormalizedNodeActionInput(
            new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.InputBuilder().build());
    }

    @Test
    public void testOutputSerialization() {
        registry.toNormalizedNodeActionOutput(new InputBuilder().setXyzzy("xyzzy").build());
        registry.toNormalizedNodeActionOutput(
            new org.opendaylight.yang.gen.v1.urn.odl.actions.norev.grp.bar.InputBuilder().build());
    }
}
