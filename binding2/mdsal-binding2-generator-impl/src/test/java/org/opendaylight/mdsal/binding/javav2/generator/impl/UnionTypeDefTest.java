/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class UnionTypeDefTest {

    @Test
    public void unionTypeResolvingTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResources(UnionTypeDefTest.class,
            "/union-test-models/abstract-topology.yang", "/ietf/ietf-inet-types.yang");

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        // TODO: implement test
    }

    @Test
    public void unionTypedefLeafrefTest() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
            "/leafref_typedef_union/bug8449.yang");
        assertNotNull(schemaContext);
        final List<Type> generateTypes = new BindingGeneratorImpl(false).generateTypes(schemaContext);
        assertNotNull(generateTypes);
        for (final Type type : generateTypes) {
            if (type.getName().equals("Cont")) {
                final GeneratedType gt = (GeneratedType) type;
                assertNotNull(gt);
                final GeneratedType refType = gt.getEnclosedTypes().iterator().next();
                for (final GeneratedProperty generatedProperty : refType.getProperties()) {
                    switch (generatedProperty.getName()) {
                        case "stringRefValue":
                            assertEquals(Types.STRING, generatedProperty.getReturnType());
                            break;
                        case "value":
                            assertEquals(Types.CHAR_ARRAY, generatedProperty.getReturnType());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
