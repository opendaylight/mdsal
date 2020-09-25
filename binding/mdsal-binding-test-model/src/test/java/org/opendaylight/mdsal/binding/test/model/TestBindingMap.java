/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.lal.norev.Foo;
import org.opendaylight.yang.gen.v1.lal.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.lal.norev.foo.Bar;
import org.opendaylight.yang.gen.v1.lal.norev.foo.BarBuilder;
import org.opendaylight.yang.gen.v1.lal.norev.foo.BarKey;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

public class TestBindingMap {
    @Test
    public void ofTest() {
        final int ONE = 1;
        final int TWO = 2;
        final String ONE_STR = "one";
        final String TWO_STR = "two";
        final BarKey BAR_KEY_ONE = new BarKey(ONE);
        final BarKey BAR_KEY_TWO = new BarKey(TWO);
        final Bar BAR_ONE = new BarBuilder().withKey(BAR_KEY_ONE).setName(ONE_STR).build();
        final Bar BAR_TWO = new BarBuilder().withKey(BAR_KEY_TWO).setName(TWO_STR).build();


        final Foo foo = new FooBuilder()
                .setBar(BindingMap.of(BAR_ONE, BAR_TWO))
                .build();
        final Map<BarKey, Bar> bar = foo.getBar();

        assertNotNull(bar);

        assertEquals(bar.get(BAR_KEY_ONE), BAR_ONE);
        assertEquals(bar.get(BAR_KEY_TWO), BAR_TWO);
    }
}
