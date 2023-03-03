/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal669Test {
    private static final JavaTypeName FOO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Foo");
    private static final JavaTypeName BAR = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Bar");
    private static final JavaTypeName BAZ = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Baz");
    private static final JavaTypeName ONE = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "One");
    private static final JavaTypeName TWO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Two");
    private static final JavaTypeName FOO_AS_STRING =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "FooAsString");
    private static final JavaTypeName UNUSED =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Unused");
    private static final JavaTypeName UNUSED_BAR =
        JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedBar");

    @Test
    void testActionsAndAugmentsTest() {
        final var models = YangParserTestUtils.parseYangResourceDirectory("/mdsal-669");
        final var runtimeTypes = new DefaultBindingRuntimeGenerator().generateTypeMapping(models);
        assertNotNull(runtimeTypes);

        final var bar = assertInstanceOf(GroupingRuntimeType.class, runtimeTypes.findSchema(BAR).orElseThrow());
        final var foo = runtimeTypes.findSchema(FOO).orElseThrow();
        assertEquals(Set.of(foo), runtimeTypes.allGroupingInstances(bar));

        final var baz = assertInstanceOf(GroupingRuntimeType.class, runtimeTypes.findSchema(BAZ).orElseThrow());
        final var one = runtimeTypes.findSchema(ONE).orElseThrow();
        final var two = runtimeTypes.findSchema(TWO).orElseThrow();
        assertEquals(Set.of(one, two), runtimeTypes.allGroupingInstances(baz));

        final var unused = assertInstanceOf(GroupingRuntimeType.class, runtimeTypes.findSchema(UNUSED).orElseThrow());
        assertEquals(Set.of(), runtimeTypes.allGroupingInstances(unused));

        final var fooAsString = assertInstanceOf(GroupingRuntimeType.class,
            runtimeTypes.findSchema(FOO_AS_STRING).orElseThrow());
        assertEquals(Set.of(), runtimeTypes.allGroupingInstances(fooAsString));

        final var unusedBar = assertInstanceOf(GroupingRuntimeType.class,
            runtimeTypes.findSchema(UNUSED_BAR).orElseThrow());
        assertEquals(Set.of(), runtimeTypes.allGroupingInstances(unusedBar));
    }
}
