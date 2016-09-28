/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yangtools.binding.generator.util.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

/**
 * Created by petko on 26.9.2016.
 */
public class GeneratorUtilTest {

    @Test
    public void createChildImportsTest() throws Exception {
        GeneratedType generatedType = mock(GeneratedType.class);
        GeneratedType enclosedType = mock(GeneratedType.class);
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();
        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
        Map<String, String> generated = GeneratorUtil.createChildImports(generatedType);
        assertNotNull(generated);
        assertTrue(generated.get("tstName").equals("tst.package"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructTest() throws Throwable {
        final Constructor constructor = GeneratorUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createImportsWithExceptionTest() throws Exception {
        GeneratorUtil.createImports(null);
    }

    @Test
    public void createImportsTest() throws Exception {
        GeneratedType generatedType = mock(GeneratedType.class);
        GeneratedTransferObject enclosedType = mock(GeneratedTransferObject.class);
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();

        MethodSignature methodSignature = mock(MethodSignature.class);
        Type type = mock(Type.class);
        AnnotationType annotationType = mock(AnnotationType.class);
        MethodSignature.Parameter parameter = mock(MethodSignature.Parameter.class);
        doReturn(ImmutableList.of(parameter)).when(methodSignature).getParameters();
        doReturn("tst.package").when(type).getPackageName();
        doReturn("tstName").when(type).getName();
        doReturn("tst.package").when(annotationType).getPackageName();
        doReturn("tstAnnotationName").when(annotationType).getName();
        doReturn(type).when(parameter).getType();
        doReturn(type).when(methodSignature).getReturnType();
        doReturn(ImmutableList.of(annotationType)).when(methodSignature).getAnnotations();
        doReturn(ImmutableList.of(methodSignature)).when(enclosedType).getMethodDefinitions();

        Constant constant = mock(Constant.class);
        doReturn(PATTERN_CONSTANT_NAME).when(constant).getName();
        doReturn(ImmutableList.of(constant)).when(enclosedType).getConstantDefinitions();

        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
        Map<String, String> generated = GeneratorUtil.createImports(generatedType);

    }
}