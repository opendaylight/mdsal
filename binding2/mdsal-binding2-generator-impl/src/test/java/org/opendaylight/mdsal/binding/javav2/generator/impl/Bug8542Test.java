/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8542Test {
    @Test
    public void testBug8542() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResource("/bug-8542/recursive-uses-augment.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("A11")) {
                assertEquals(
                    "org.opendaylight.mdsal.gen.javav2.yang.test.uses.augment.recursive.rev170519.data.d",
                    type.getPackageName());
            } else if (type.getName().equals("B11")) {
                assertEquals(
                    "org.opendaylight.mdsal.gen.javav2.yang.test.uses.augment.recursive.rev170519.data.d.a1",
                    type.getPackageName());
            } else if (type.getName().equals("C11")) {
                assertEquals(
                    "org.opendaylight.mdsal.gen.javav2.yang.test.uses.augment.recursive.rev170519."
                    + "data.d.a1.b1", type.getPackageName());
            }
        }
    }
}
