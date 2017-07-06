/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.renderers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;

public class BuilderRendererTest {
    private static final String TEST = "test";

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + TEST);
        final String generateToString = genToString(genType);
        assertTrue(generateToString.contains("java.lang.String toString() {"));
        assertTrue(generateToString.contains("java.lang.String name = \"test [\";"));
        assertTrue(generateToString.contains("java.lang.StringBuilder builder = new java.lang.StringBuilder(name);"));
        assertTrue(generateToString.contains("if (_test != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test=\");"));
        assertTrue(generateToString.contains("builder.append(_test);"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }
;
    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType(TEST);
        final String generateToString = genToString(genType);
        assertTrue(generateToString.contains("java.lang.String toString() {"));
        assertTrue(generateToString.contains("java.lang.String name = \"test [\";"));
        assertTrue(generateToString.contains("java.lang.StringBuilder builder = new java.lang.StringBuilder(name);"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + TEST);
        final String generateToString = genToString(genType);
        assertTrue(generateToString.contains("java.lang.String toString() {"));
        assertTrue(generateToString.contains("java.lang.String name = \"test [\";"));
        assertTrue(generateToString.contains("java.lang.StringBuilder builder = new java.lang.StringBuilder(name);"));
        assertTrue(generateToString.contains("if (_test1 != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test1=\");"));
        assertTrue(generateToString.contains("builder.append(_test1);"));
        assertTrue(generateToString.contains("builder.append(\", \");"));
        assertTrue(generateToString.contains("if (_test2 != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test2=\");"));
        assertTrue(generateToString.contains("builder.append(_test2);"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType(TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType);
        assertTrue(generateToString.contains("java.lang.String toString() {"));
        assertTrue(generateToString.contains("java.lang.String name = \"test [\";"));
        assertTrue(generateToString.contains("java.lang.StringBuilder builder = new java.lang.StringBuilder(name);"));
        assertTrue(generateToString.contains("builder.append(\"augmentation=\");"));
        assertTrue(generateToString.contains("builder.append(augmentation.values());"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType);
        assertTrue(generateToString.contains("java.lang.String toString() {"));
        assertTrue(generateToString.contains("java.lang.String name = \"test [\";"));
        assertTrue(generateToString.contains("java.lang.StringBuilder builder = new java.lang.StringBuilder(name);"));
        assertTrue(generateToString.contains("if (_test != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test=\");"));
        assertTrue(generateToString.contains("builder.append(_test);"));
        assertTrue(generateToString.contains("builder.append(\", \");"));
        assertTrue(generateToString.contains("builder.append(\"augmentation=\");"));
        assertTrue(generateToString.contains("builder.append(augmentation.values());"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains("if (_test1 != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test1=\");"));
        assertTrue(generateToString.contains("builder.append(_test1);"));
        assertTrue(generateToString.contains("builder.append(\", \");"));
        assertTrue(generateToString.contains("if (_test2 != null) {"));
        assertTrue(generateToString.contains("builder.append(\"_test2=\");"));
        assertTrue(generateToString.contains("builder.append(_test2);"));
        assertTrue(generateToString.contains("builder.append(\", \");"));
        assertTrue(generateToString.contains("builder.append(\"augmentation=\");"));
        assertTrue(generateToString.contains("builder.append(augmentation.values());"));
        assertTrue(generateToString.contains("return builder.append(']').toString();"));
    }

    private void mockAugment(final GeneratedType genType) {
        final List<Type> impls = new ArrayList<>();
        final Type impl = mock(Type.class);
        doReturn("org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable").when(impl)
                .getFullyQualifiedName();
        impls.add(impl);
        doReturn(impls).when(genType).getImplements();
    }

    private GeneratedType mockGenTypeMoreMeth(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();
        doReturn(new GeneratedTypeBuilderImpl(new StringBuilder(methodeName).append("test").toString(), methodeName,
                new ModuleContext())
                .toInstance()).when(genType).getParentTypeForBuilder();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            final MethodSignature methSign = mockMethSign(methodeName + (i + 1));
            listMethodSign.add(methSign);
        }
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private String genToString(final GeneratedType genType) {
        final BuilderRenderer bt = new BuilderRenderer(genType);
        return bt.body();
    }

    private GeneratedType mockGenType(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();
        doReturn(new GeneratedTypeBuilderImpl(new StringBuilder(methodeName).append("test").toString(), methodeName,
                new ModuleContext())
                .toInstance()).when(genType).getParentTypeForBuilder();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        final MethodSignature methSign = mockMethSign(methodeName);
        listMethodSign.add(methSign);
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = mock(Type.class);
        doReturn(TEST).when(methType).getName();
        doReturn(TEST).when(methType).getPackageName();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
