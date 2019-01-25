/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.TYPE_OBJECT;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal406TypeObjectTest {

    @Test
    public void typeObjectTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResources(ExtendedTypedefTest.class,
                "/type-provider/test.yang", "/ietf/ietf-inet-types.yang");

        final List<Type> generateTypes = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(generateTypes);

        final Type typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyBinary")).findFirst().get();

        assertTrue(typedefType instanceof GeneratedType);
        assertNotNull(((GeneratedType)  typedefType).getImplements());
        Type objectType = ((GeneratedType)  typedefType).getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yangtools.yang.binding.TypeObject")).findAny().get();
        assertEquals(TYPE_OBJECT, objectType);
    }
}
