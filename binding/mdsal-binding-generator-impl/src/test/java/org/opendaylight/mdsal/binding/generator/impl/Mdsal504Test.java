/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal504Test {
    @Test
    public void testDerefLeafref() {
        final List<Type> types = new BindingGeneratorImpl().generateTypes(
                YangParserTestUtils.parseYangResource("/mdsal504.yang"));
        assertNotNull(types);
        assertEquals(7, types.size());

        final Type fifth = types.get(4);
        assertThat(fifth, isA(GeneratedType.class));
        final List<MethodSignature> methods = ((GeneratedType) fifth).getMethodDefinitions();
        assertEquals(4, methods.size());
        final MethodSignature getType = methods.get(2);
        assertEquals("getType", getType.getName());
        final Type retType = getType.getReturnType();
        assertThat(retType, isA(ParameterizedType.class));
        final ParameterizedType retParam = (ParameterizedType) retType;
        assertEquals(Types.typeForClass(Class.class), retParam.getRawType());

        final Type[] args = retParam.getActualTypeArguments();
        assertEquals(1, args.length);
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal504.norev", "TargetBase"),
            args[0].getIdentifier());
    }
}
