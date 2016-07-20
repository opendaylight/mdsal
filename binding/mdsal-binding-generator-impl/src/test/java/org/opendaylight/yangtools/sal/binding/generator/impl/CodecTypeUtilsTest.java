/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public class CodecTypeUtilsTest {

    @Test
    public void newIdentifiableItem() throws Exception {
        assertNotNull(CodecTypeUtils.newIdentifiableItem(Identifiable.class, mock(Identifier.class)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void privateConstructTest() throws Throwable {
        final Constructor constructor = CodecTypeUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}