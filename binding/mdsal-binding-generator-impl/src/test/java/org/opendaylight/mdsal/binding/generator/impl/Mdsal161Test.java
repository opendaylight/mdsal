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

import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal161Test {
    /**
     * Test if leaves with inner union type defined in groupings can be used as list keys at the place of instantiation.
     */
    @Test
    public void mdsal161Test() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal161.yang");
        final Collection<Type> types = DefaultBindingGenerator.generateFor(context);
        assertNotNull(types);
        assertEquals(23, types.size());

        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpExtKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithGrpTypedefKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpExtKey");
        assertKeyStructure(types, "org.opendaylight.yang.gen.v1.mdsal161.norev.WithoutGrpTypedefKey");
    }

    private static void assertKeyStructure(final Collection<Type> types, final String className) {
        final Optional<Type> optType = types.stream().filter(t -> t.getFullyQualifiedName().equals(className))
                .findFirst();
        assertTrue("Type for " + className + " not found", optType.isPresent());

        final Type type = optType.get();
        assertTrue(type instanceof GeneratedTransferObject);
        final GeneratedTransferObject gto = (GeneratedTransferObject) type;
        assertEquals(2, gto.getEqualsIdentifiers().size());
    }
}
