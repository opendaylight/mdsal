/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static java.util.Arrays.stream;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.lang.reflect.Field;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import org.junit.Test;

public class SourceCodeGeneratorFactoryTest {

    private static final SourceCodeGeneratorFactory FACTORY = new SourceCodeGeneratorFactory();
    private SourceCodeGenerator generator;
    private String propKey;

    @Test
    public void basicFactoryTest() throws Exception {
        final Field propField = SourceCodeGeneratorFactory.class.getDeclaredField("GENERATE_CODEC_SOURCE_PROP");
        propField.setAccessible(true);
        propKey = (String) propField.get(FACTORY);
        final String propValue;
        final boolean present;

        propValue = System.getProperty(propKey, null);
        present = propValue != null;

        testWithPropertyPresent();
        testWithoutPropertyPresent();

        if (present) {
            System.setProperty(propKey, propValue);
        } else {
            System.clearProperty(propKey);
        }
    }

    private void testWithPropertyPresent() throws Exception {
        System.clearProperty(propKey);
        System.setProperty(propKey, "true");
        generator = FACTORY.getInstance(null);
        assertTrue(generator instanceof DefaultSourceCodeGenerator);
    }

    private void testWithoutPropertyPresent() throws Exception {
        System.clearProperty(propKey);
        generator = FACTORY.getInstance(null);
        assertTrue(generator instanceof NullSourceCodeGenerator);
    }

    @Test
    public void nullSourceCodeGenTest() throws Exception {
        generator = new NullSourceCodeGenerator();
        generator.appendField(null, null);
    }

    @Test
    public void defaultSourceCodeGenTest() throws Exception {
        final File dir = new File("testDir");
        assertTrue(cleanup(dir));

        generator = new DefaultSourceCodeGenerator(dir.getName());
        final CtClass ctClass = mock(CtClass.class, CALLS_REAL_METHODS);
        doReturn(false).when(ctClass).isFrozen();
        ctClass.setName("TestClass");
        final ClassPool classPool = mock(ClassPool.class);
        doReturn(ctClass).when(classPool).get((String) any());
        doNothing().when(ctClass).setName("TestClass");
        doReturn(ctClass).when(ctClass).getSuperclass();
        doReturn(new CtClass[] {ctClass,ctClass}).when(ctClass).getInterfaces();
        doReturn(classPool).when(ctClass).getClassPool();
        doReturn(false).when(ctClass).isArray();
        doReturn(false).when(ctClass).isPrimitive();
        doReturn(AccessFlag.toModifier(AccessFlag.PUBLIC)).when(ctClass).getModifiers();
        final ClassFile classFile = new ClassFile(false,"test", null);
        doReturn(classFile).when(ctClass).getClassFile2();
        doReturn(false).when(ctClass).isFrozen();
        doReturn("testClass").when(ctClass).getName();
        final CtField ctField = mock(CtField.class);
        doReturn(AccessFlag.toModifier(AccessFlag.PUBLIC)).when(ctField).getModifiers();
        doReturn(ctClass).when(ctField).getType();
        doReturn("testField").when(ctField).getName();
        final CtMethod ctMethod = new CtMethod(ctClass,"method", new CtClass[] { ctClass , ctClass }, ctClass);

        generator.appendField(ctField, "b");
        generator.appendMethod(ctMethod, "c");
        generator.outputGeneratedSource(ctClass);

        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        assertFalse(ImmutableList.of(dir.listFiles()).isEmpty());

        assertTrue(cleanup(dir));
    }

    private boolean cleanup(File dir) {
        if (!dir.exists()) return true;

        stream(dir.listFiles()).forEach(file -> file.delete());
        return dir.delete();
    }
}