/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.junit.Test;

@Deprecated
public class JavassistUtilsTest {

    @Test
    public void forClassPool() throws CannotCompileException, NotFoundException {
        final JavassistUtils javassistUtils = JavassistUtils.forClassPool(ClassPool.getDefault());
        final ClassGenerator classGenerator = mock(ClassGenerator.class);
        doNothing().when(classGenerator).process(any());

        final ClassCustomizer classCustomizer = mock(ClassCustomizer.class);
        doNothing().when(classCustomizer).customizeClass(any());
        assertNotNull(javassistUtils.instantiatePrototype("javassist.CtNewClass", "leWut", classCustomizer));
    }

    @Test
    public void privateConstructTest() throws Exception {
        assertFalse(JavassistUtils.class.getDeclaredConstructor(ClassPool.class).isAccessible());
    }
}