/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.opendaylight.mdsal.binding.java.api.generator.GeneratorUtil.createImports;
import static org.opendaylight.mdsal.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyAnyxmlEffectiveStatement;

public class GeneratorUtilTest {
    private static final JavaTypeName ANNOTATION = JavaTypeName.create("tst.package", "tstAnnotationName");
    private static final JavaTypeName PARAMETERIZED_TYPE = JavaTypeName.create("tst.package", "tstParametrizedType");
    private static final JavaTypeName TYPE = JavaTypeName.create("tst.package", "tstName");

    private final GeneratedType generatedType = mock(GeneratedType.class);
    private final GeneratedTransferObject enclosedType = mock(GeneratedTransferObject.class);
    private final MethodSignature methodSignature = mock(MethodSignature.class);
    private final Type type = mock(Type.class);
    private final AnnotationType annotationType = mock(AnnotationType.class);
    private final MethodSignature.Parameter parameter = mock(MethodSignature.Parameter.class);
    private final GeneratedProperty property = mock(GeneratedProperty.class);
    private final ParameterizedType parameterizedType = mock(ParameterizedType.class);

    @Before
    public void setUp() {
        reset(enclosedType);

        doReturn("tst.package").when(parameterizedType).getPackageName();
        doReturn("tstParametrizedType").when(parameterizedType).getName();
        doReturn(PARAMETERIZED_TYPE).when(parameterizedType).getIdentifier();
        doReturn("tst.package").when(type).getPackageName();
        doReturn("tstName").when(type).getName();
        doReturn(TYPE).when(type).getIdentifier();
        doReturn(parameterizedType).when(property).getReturnType();
        doReturn(new Type[] { type }).when(parameterizedType).getActualTypeArguments();
        doReturn(ImmutableList.of(property)).when(enclosedType).getProperties();
        doReturn(Boolean.TRUE).when(property).isReadOnly();
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();
        doReturn(TYPE).when(enclosedType).getIdentifier();

        doReturn(ImmutableList.of(parameter)).when(methodSignature).getParameters();

        doReturn("tst.package").when(annotationType).getPackageName();
        doReturn("tstAnnotationName").when(annotationType).getName();
        doReturn(ANNOTATION).when(annotationType).getIdentifier();

        doReturn(type).when(parameter).getType();
        doReturn(type).when(methodSignature).getReturnType();
        doReturn(ImmutableList.of(annotationType)).when(methodSignature).getAnnotations();
        doReturn(ImmutableList.of(methodSignature)).when(enclosedType).getMethodDefinitions();

        final Constant constant = mock(Constant.class);
        doReturn(PATTERN_CONSTANT_NAME).when(constant).getName();
        doReturn(ImmutableList.of(constant)).when(enclosedType).getConstantDefinitions();

        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
    }

    @Test
    public void createChildImportsTest() throws Exception {
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();
        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
        final Map<String, String> generated = GeneratorUtil.createChildImports(generatedType);
        assertNotNull(generated);
        assertTrue(generated.get("tstName").equals("tst.package"));
    }

    public void constructTest() throws ReflectiveOperationException {
        final Constructor<GeneratorUtil> constructor = GeneratorUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail();
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createImportsWithExceptionTest() throws Exception {
        GeneratorUtil.createImports(null);
    }

    @Test
    public void createImportsTest() throws Exception {
        final Map<String, JavaTypeName> generated = createImports(generatedType);
        assertNotNull(generated);
        assertEquals(JavaTypeName.create("tst.package", "tstAnnotationName"), generated.get("tstAnnotationName"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isConstantToExceptionTest() throws Exception {
        GeneratorUtil.isConstantInTO(null, null);
    }

    @Test
    public void isConstantToTest() throws Exception {
        final Constant constant = mock(Constant.class);
        doReturn("tst2").when(constant).getName();
        doReturn(ImmutableList.of(constant)).when(enclosedType).getConstantDefinitions();
        assertFalse(GeneratorUtil.isConstantInTO("tst", enclosedType));
    }

    @Test
    public void getPropertiesOfAllParentsTest() throws Exception {
        final GeneratedTransferObject superType = mock(GeneratedTransferObject.class);
        doReturn(enclosedType).when(superType).getSuperType();
        assertTrue(GeneratorUtil.getPropertiesOfAllParents(superType).contains(property));
    }

    @Test
    public void getExplicitTypeTest() throws Exception {
        assertEquals(annotationType.getName(), GeneratorUtil.getExplicitType(
                generatedType, annotationType, createImports(generatedType)));

        assertTrue(GeneratorUtil.getExplicitType(generatedType, parameterizedType,
                createImports(generatedType)).contains(parameterizedType.getName()));
    }

    /**
     * Test that type which is NOT container is NOT recognized as non-presence container.
     */
    @Test
    public void nonContainerIsNonPresenceContainerTest() {
        final EmptyAnyxmlEffectiveStatement schemaNode = new EmptyAnyxmlEffectiveStatement(mock(AnyxmlStatement.class),
                mock(Immutable.class), 0, mock(AnyxmlSchemaNode.class));
        final ModuleEffectiveStatement effective = mock(ModuleEffectiveStatement.class);
        doReturn(mock(ModuleStatement.class)).when(effective).getDeclared();
        final Module module = mock(Module.class);
        doReturn(effective).when(module).asEffectiveStatement();
        final Optional<YangSourceDefinition> definition = YangSourceDefinition.of(module, schemaNode);
        doReturn(definition).when(generatedType).getYangSourceDefinition();

        assertFalse(GeneratorUtil.isNonPresenceContainer(generatedType));
    }

    /**
     * Test that presence container is NOT recognized as non-presence container.
     */
    @Test
    public void presenceContainerIsNonPresenceContainerTest() {
        final int presenceFlags = 0x0080;
        final ContainerSchemaNode schemaNode = new ContainerEffectiveStatementImpl(mock(ContainerStatement.class),
                ImmutableList.of(), mock(Immutable.class), presenceFlags, null);
        final ModuleEffectiveStatement effective = mock(ModuleEffectiveStatement.class);
        doReturn(mock(ModuleStatement.class)).when(effective).getDeclared();
        final Module module = mock(Module.class);
        doReturn(effective).when(module).asEffectiveStatement();
        final Optional<YangSourceDefinition> definition = YangSourceDefinition.of(module, schemaNode);
        doReturn(definition).when(generatedType).getYangSourceDefinition();

        assertFalse(GeneratorUtil.isNonPresenceContainer(generatedType));
    }

    /**
     * Test that non-presence container IS recognized as non-presence container.
     */
    @Test
    public void nonPresenceContainerIsNonPresenceContainerTest() {
        final ContainerSchemaNode schemaNode = new ContainerEffectiveStatementImpl(mock(ContainerStatement.class),
                ImmutableList.of(), mock(Immutable.class), 0, null);
        final ModuleEffectiveStatement effective = mock(ModuleEffectiveStatement.class);
        doReturn(mock(ModuleStatement.class)).when(effective).getDeclared();
        final Module module = mock(Module.class);
        doReturn(effective).when(module).asEffectiveStatement();
        final Optional<YangSourceDefinition> definition = YangSourceDefinition.of(module, schemaNode);
        doReturn(definition).when(generatedType).getYangSourceDefinition();

        assertTrue(GeneratorUtil.isNonPresenceContainer(generatedType));
    }
}