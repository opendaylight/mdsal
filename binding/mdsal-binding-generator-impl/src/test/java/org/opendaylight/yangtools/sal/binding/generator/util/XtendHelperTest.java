/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Constructor;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class XtendHelperTest {

    @Test
    public void getTypesTest() throws Exception {
        final UnionTypeDefinition unionTypeDefinition = mock(UnionTypeDefinition.class);
        doReturn(null).when(unionTypeDefinition).getTypes();
        XtendHelper.getTypes(unionTypeDefinition);
        verify(unionTypeDefinition).getTypes();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void privateConstructTest() throws Throwable {
        final Constructor constructor = XtendHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}