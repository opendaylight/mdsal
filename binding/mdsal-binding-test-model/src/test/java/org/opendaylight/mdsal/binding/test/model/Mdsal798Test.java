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
import org.opendaylight.mdsal.binding.test.model.mock.List12KeyMock;
import org.opendaylight.mdsal.binding.test.model.mock.List12Mock;
import org.opendaylight.mdsal.binding.test.model.mock.List1KeyMock;
import org.opendaylight.mdsal.binding.test.model.mock.List1Mock;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.WildcardedInstanceIdentifierBuilder;

public class Mdsal798Test {

    @Test
    public void test() {
        assertThrows(IllegalArgumentException.class, () -> InstanceIdentifier.builder(List1Mock.class)
                .child(List12Mock.class, new List12KeyMock(
                    Integer.valueOf("78"))));
        final var keyedBuilder = InstanceIdentifier.builder(List1Mock.class, new List1KeyMock("placeholder"));
        assertTrue(keyedBuilder instanceof KeyedInstanceIdentifierBuilder<List1Mock>);
        assertThrows(IllegalArgumentException.class, () -> keyedBuilder.child(List12Mock.class));
        assertTrue(keyedBuilder.wildcardChild(List12Mock.class)
                instanceof WildcardedInstanceIdentifierBuilder<List12Mock>);
        assertTrue(keyedBuilder.child(List12Mock.class, new List12KeyMock(55))
                instanceof KeyedInstanceIdentifierBuilder<List12Mock>);
    }
}
