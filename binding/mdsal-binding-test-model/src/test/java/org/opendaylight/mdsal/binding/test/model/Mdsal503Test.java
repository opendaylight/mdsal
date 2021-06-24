/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal503.norev.BarBuilder;
import org.opendaylight.yang.gen.v1.mdsal503.norev.Foo;

public class Mdsal503Test {
    @Test
    public void testEnforceBinaryLength() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new BarBuilder().setBaz(new Foo(new byte[0])).build());
        assertThat(ex.getMessage(), startsWith("Invalid length: "));
    }
}
