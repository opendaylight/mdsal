/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Mdsal361Test {

    @Test
    public void mdsal361Test() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal361.yang");

        final List<Type> generateTypes = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(generateTypes);
        assertEquals(4, generateTypes.size());
        for (final Type type : generateTypes) {
            if (type.getName().equals("PceId")) {
                final GeneratedType gt = (GeneratedType) type;
                assertNotNull(gt);
                assertEquals(2, gt.getProperties().size());
            }

            if (type.getName().equals("PceId2")) {
                final GeneratedType gt = (GeneratedType) type;
                assertNotNull(gt);
                assertEquals(2, gt.getEnumerations().size());
                assertEquals(2, gt.getProperties().size());
            }
        }
    }
}
