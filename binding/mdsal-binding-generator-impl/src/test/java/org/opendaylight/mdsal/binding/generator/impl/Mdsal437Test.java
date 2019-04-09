/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal437Test {

    @Test
    public void generateAnyxmlTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal437.yang");

        final List<Type> types = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(types);
        assertEquals(7, types.size());

        assertXmlNode(types, "", "Any");
        assertXmlNode(types, ".cont", "Cont");
        assertXmlNode(types, ".grp", "Grp");

    }

    private static void assertXmlNode(final List<Type> types, final String pkg, final String name) {
        final JavaTypeName typeName = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal437.norev" + pkg, name);
        final Optional<Type> optType = types.stream().filter(t -> typeName.equals(t.getIdentifier())).findFirst();
        assertTrue(optType.isPresent());
        final Type type = optType.get();
        assertTrue(type instanceof GeneratedType);
        final GeneratedType genType = (GeneratedType) type;
        final Iterator<Type> it = genType.getImplements().iterator();
        final Type first = it.next();
        assertTrue(first instanceof ParameterizedType);
        assertEquals(JavaTypeName.create(OpaqueObject.class), ((ParameterizedType) first).getRawType().getIdentifier());

        final Type second = it.next();
        assertTrue(second instanceof ParameterizedType);
        assertEquals(JavaTypeName.create(ChildOf.class), ((ParameterizedType) second).getRawType().getIdentifier());

        assertFalse(it.hasNext());
    }
}
