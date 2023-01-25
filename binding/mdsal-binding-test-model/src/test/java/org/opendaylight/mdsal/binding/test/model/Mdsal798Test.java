/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal798.norev.List1;
import org.opendaylight.yang.gen.v1.mdsal798.norev.List1Key;
import org.opendaylight.yang.gen.v1.mdsal798.norev.list1.List12;
import org.opendaylight.yang.gen.v1.mdsal798.norev.list1.List12Key;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifierBuilderImpl;
import org.opendaylight.yangtools.yang.binding.WildcardedInstanceIdentifierBuilderImpl;

public class Mdsal798Test {

    @Test
    public void test() {
        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.builder(List1.class)
                .child(List12.class, new List12Key(Integer.valueOf("78"))));
        final var keyedBuilder = InstanceIdentifier.builder(List1.class, new List1Key("placeholder"));
        assertTrue(keyedBuilder instanceof KeyedInstanceIdentifierBuilderImpl<?, ?>);
        assertThrows(IllegalArgumentException.class, () -> keyedBuilder.child(List12.class));
        assertTrue(keyedBuilder.wildcardChild(List12.class) instanceof WildcardedInstanceIdentifierBuilderImpl<?>);
        assertTrue(keyedBuilder.child(List12.class, new List12Key(55))
                instanceof KeyedInstanceIdentifierBuilderImpl<?, ?>);
    }
}
