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
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal320Test {

    @Test
    public void mdsal320Test() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/mdsal320.yang");

        final List<Type> generateTypes = new BindingGeneratorImpl().generateTypes(context);
        assertNotNull(generateTypes);
        assertEquals(4, generateTypes.size());

        final Type fooType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.odl.yt320.norev.Foo")).findFirst().get();
        assertTrue(fooType instanceof GeneratedType);
        final GeneratedType foo = (GeneratedType) fooType;

        GeneratedTransferObject bar = null;
        GeneratedTransferObject bar1 = null;
        for (GeneratedType enc : foo.getEnclosedTypes()) {
            switch (enc.getName()) {
                case "Bar":
                    assertTrue(enc instanceof GeneratedTransferObject);
                    bar = (GeneratedTransferObject) enc;
                    break;
                case "Bar$1":
                    assertTrue(enc instanceof GeneratedTransferObject);
                    bar1 = (GeneratedTransferObject) enc;
                    break;
                default:
                    throw new IllegalStateException("Unexpected type " + enc);
            }
        }
        assertNotNull(bar);
        assertTrue(bar.isUnionType());
        assertNotNull(bar1);
        assertTrue(bar1.isUnionType());

        final Iterator<MethodSignature> fooMethods = foo.getMethodDefinitions().iterator();
        assertTrue(fooMethods.hasNext());
        final MethodSignature getImplementedInterface = fooMethods.next();
        asserEquals(BindingMapping.DATA_CONTAINER_GET_IMPLEMENTED_INTERFACE_NAME, getImplementedInterface.getName());

        assertTrue(fooMethods.hasNext());
        final MethodSignature getBar = fooMethods.next();
        assertFalse(fooMethods.hasNext());

        final Type getBarType = getBar.getReturnType();
        assertTrue(getBarType instanceof GeneratedTransferObject);
        final GeneratedTransferObject getBarTO = (GeneratedTransferObject) getBarType;
        assertTrue(getBarTO.isUnionType());
        assertEquals(bar, getBarTO);

        final GeneratedProperty bar1Prop = bar.getProperties().stream().filter(prop -> "bar$1".equals(prop.getName()))
                .findFirst().get();
        final Type bar1PropRet = bar1Prop.getReturnType();
        assertEquals(bar1, bar1PropRet);
    }

    private void asserEquals(final String dataContainerGetImplementedInterfaceName, final String name) {
        // TODO Auto-generated method stub

    }
}
