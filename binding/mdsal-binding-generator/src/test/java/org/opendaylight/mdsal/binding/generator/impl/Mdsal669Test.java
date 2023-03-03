/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal669Test {
    private static final JavaTypeName FOO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Foo");
    private static final JavaTypeName BAR = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Bar");
    private static final JavaTypeName BAZ = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Baz");
    private static final JavaTypeName ONE = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "One");
    private static final JavaTypeName TWO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Two");
    private static final JavaTypeName UNUSED = JavaTypeName
            .create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Unused");

    @Test
    public void testActionsAndAugmentsTest() {
        final var models = YangParserTestUtils.parseYangResourceDirectory("/mdsal-669");
        final var runtimeTypes = new DefaultBindingRuntimeGenerator().generateTypeMapping(models);
        Assert.assertNotNull(runtimeTypes);

        var barInstances = runtimeTypes.allGroupingInstance(BAR);
        Assert.assertEquals(1, barInstances.size());
        Assert.assertTrue(barInstances.contains(FOO));

        var bazInstances = runtimeTypes.allGroupingInstance(BAZ);
        Assert.assertEquals(2, bazInstances.size());
        Assert.assertTrue(bazInstances.contains(ONE));
        Assert.assertTrue(bazInstances.contains(TWO));

        var unusedInstances = runtimeTypes.allGroupingInstance(UNUSED);
        Assert.assertTrue(unusedInstances.isEmpty());
    }
}
