/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class UnionTypeDefTest {
    @Test
    public void unionTypeResolvingTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            UnionTypeDefTest.class, "/union-test-models/abstract-topology.yang", "/ietf-models/ietf-inet-types.yang"));

        assertNotNull("genTypes is null", genTypes);
        assertEquals(33, genTypes.size());

        // TODO: implement test
    }

    @Test
    public void unionTypedefLeafrefTest() {
        final List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/leafref_typedef_union/bug8449.yang"));
        assertNotNull(generateTypes);
        for (final GeneratedType type : generateTypes) {
            if (type.getName().equals("Cont")) {
                final GeneratedType refType = type.getEnclosedTypes().iterator().next();
                for (final GeneratedProperty generatedProperty : refType.getProperties()) {
                    switch (generatedProperty.getName()) {
                        case "stringRefValue":
                            assertEquals(Types.STRING, generatedProperty.getReturnType());
                            break;
                        default:
                            // ignore
                    }
                }
            }
        }
    }
}
