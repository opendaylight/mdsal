/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.mdsal.sal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.sal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.sal.binding.model.api.Type;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.yang.types.BaseYangTypes;

public class BaseTypeProvider {

    @Test
    public void test() {
        TypeProvider provider = BaseYangTypes.BASE_YANG_TYPES_PROVIDER;

        Type stringType = provider.javaTypeForYangType("string");
        assertEquals("java.lang", stringType.getPackageName());
        assertEquals("String", stringType.getName());
        assertTrue(stringType instanceof ConcreteType);
        ParameterizedType stringBooleanMap = Types.mapTypeFor(
                provider.javaTypeForYangType("string"),
                provider.javaTypeForYangType("boolean"));
        assertTrue(!(stringBooleanMap instanceof ConcreteType));
        assertEquals("java.util", stringBooleanMap.getPackageName());
        assertEquals("Map", stringBooleanMap.getName());
        assertEquals(2, stringBooleanMap.getActualTypeArguments().length);
    }
}
