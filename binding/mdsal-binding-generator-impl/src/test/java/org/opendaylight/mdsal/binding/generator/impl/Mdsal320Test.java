/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal320Test {

    @Test
    public void mdsal320Test() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal320.yang");

        final List<Type> generateTypes = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(generateTypes);
        assertEquals(3, generateTypes.size());

        final Type fooType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.odl.yt320.norev.Foo")).findFirst().get();

        assertTrue(fooType instanceof GeneratedTransferObject);
        final GeneratedTransferObject gto = (GeneratedTransferObject) fooType;
        final Iterator<GeneratedProperty> it = gto.getEqualsIdentifiers().iterator();
        final GeneratedProperty special = it.next();
        final GeneratedProperty general = it.next();
        assertFalse(it.hasNext());

        assertEquals("mplsLabelGeneralUse", general.getName());
        assertEquals("org.opendaylight.yang.gen.v1.mdsal269.rev180130.MplsLabelGeneralUse",
            general.getReturnType().toString());

        assertEquals("mplsLabelSpecialPurpose", special.getName());
        assertEquals("Type (java.lang.Class)", special.getReturnType().toString());
    }
}
