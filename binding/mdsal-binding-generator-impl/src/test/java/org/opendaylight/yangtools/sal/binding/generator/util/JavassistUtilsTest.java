/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;
import org.junit.Test;

public class JavassistUtilsTest {

    @Test
    public void forClassPool() throws Exception {
        final JavassistUtils javassistUtils = JavassistUtils.forClassPool(ClassPool.getDefault());
        final ClassGenerator classGenerator = mock(ClassGenerator.class);
        doNothing().when(classGenerator).process(any());
        final CtClass ctInterface = javassistUtils.createClass("TestInterface", classGenerator);
        ctInterface.setModifiers(AccessFlag.INTERFACE);
        final CtClass ctClass = javassistUtils.createClass("TestClass", ctInterface, classGenerator);
        javassistUtils.ensureClassLoader(ctClass.getClass());
        assertNotNull(ctClass);
        assertNotNull(javassistUtils.get(ClassPool.getDefault(), ctClass.getClass()));

        CtField ctField = javassistUtils.field(ctClass, "testField", Object.class);
        assertEquals(ctField, ctClass.getField("testField"));
        ctField = javassistUtils.staticField(ctClass, "testStaticField", Object.class);
        assertEquals(ctField, ctClass.getField("testStaticField"));

        final MethodGenerator methodGenerator = mock(MethodGenerator.class);
        doNothing().when(methodGenerator).process(any());
        javassistUtils.method(ctClass, Void.class, "testMethod", Object.class, methodGenerator);
        javassistUtils.method(ctInterface, Void.class, "testInterfaceMethod", Object.class, methodGenerator);
        javassistUtils.method(ctClass, Void.class, "testMethod2", ImmutableList.of(Object.class), methodGenerator);
        javassistUtils.staticMethod(ctClass, Void.class, "testStaticMethod", Object.class, methodGenerator);
        javassistUtils.implementMethodsFrom(ctClass, ctInterface, methodGenerator);
        assertNotNull(ctClass.getDeclaredMethod("testMethod"));
        assertNotNull(ctClass.getDeclaredMethod("testMethod2"));
        assertNotNull(ctClass.getDeclaredMethod("testStaticMethod"));
        assertNotNull(ctClass.getDeclaredMethod("testInterfaceMethod"));

        final ClassCustomizer classCustomizer = mock(ClassCustomizer.class);
        doNothing().when(classCustomizer).customizeClass(any());
        assertNotNull(javassistUtils.instantiatePrototype("javassist.CtNewClass", "leWut", classCustomizer));
    }

    @Test
    public void privateConstructTest() throws Exception {
        assertFalse(JavassistUtils.class.getDeclaredConstructor(ClassPool.class).isAccessible());
    }
}