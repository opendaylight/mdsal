/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.base.Optional;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AugmentToGenTypeTest {

    @SuppressWarnings("unchecked")
    @Test(expected = UnsupportedOperationException.class)
    public void constructorTest() throws Throwable {
        final Constructor<AugmentToGenType> constructor =
                (Constructor<AugmentToGenType>) AugmentToGenType.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        final Object[] objs = {};
        try {
            constructor.newInstance(objs);
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void generateNullModuleTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, SchemaContext.class, TypeProvider.class, Map.class, Map.class, boolean.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("generate", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        final Object[] args = { m, context, typeProvider, genCtx, genTypeBuilders, false };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Module reference cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void generateNullModuleNameTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, SchemaContext.class, TypeProvider.class, Map.class, Map.class, boolean.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("generate", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = mock(Module.class);
        when(m.getName()).thenReturn(null);

        final Object[] args = { m, context, typeProvider, genCtx, genTypeBuilders, false };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Module name cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void generateNullModuleAugmentationsTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, SchemaContext.class, TypeProvider.class, Map.class, Map.class, boolean.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("generate", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("name");
        when(m.getAugmentations()).thenReturn(null);

        final Object[] args = { m, context, typeProvider, genCtx, genTypeBuilders, false };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("Augmentations Set cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void generateWithoutAugmentationsTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, SchemaContext.class, TypeProvider.class, Map.class, Map.class, boolean.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("generate", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext context = YangParserTestUtils.parseYangSource("/generator/test.yang");
        final TypeProvider typeProvider = new TypeProviderImpl(context);
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Object[] args =
                { context.getModules().iterator().next(), context, typeProvider, genCtx, genTypeBuilders, false };
        final Map invoke = (Map) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(invoke);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void generateWithAugmentationsTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, SchemaContext.class, TypeProvider.class, Map.class, Map.class, boolean.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("generate", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext context = YangParserTestUtils.parseYangSource("/generator/test-augm.yang");
        final TypeProvider typeProvider = new TypeProviderImpl(context);
        final Map<Module, ModuleContext> genCtx = mock(Map.class);
        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext moduleContext = new ModuleContext();
        moduleContexts.add(moduleContext);
        final QName create = QName.create("urn:test:simple:test", "2017-02-06", "my-cont");
        final SchemaNode schemaNode = mock(SchemaNode.class);
        when(schemaNode.getPath()).thenReturn(SchemaPath.create(true, create));
        moduleContext.addChildNodeType(schemaNode, new GeneratedTypeBuilderImpl("test", "Test"));
        when(genCtx.values()).thenReturn(moduleContexts);
        when(genCtx.get(context.getModules().iterator().next())).thenReturn(moduleContext);
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Object[] args =
                { context.getModules().iterator().next(), context, typeProvider, genCtx, genTypeBuilders, false };
        final Map invoke = (Map) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(invoke);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void resolveAugmentationsNullModuleTest() throws Exception {
        final Class[] parameterTypes = { Module.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("resolveAugmentations", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module m = null;

        final Object[] args = { m };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Module reference cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void resolveAugmentationsNullAugmentationsTest() throws Exception {
        final Class[] parameterTypes = { Module.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("resolveAugmentations", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module m = mock(Module.class);
        when(m.getAugmentations()).thenReturn(null);

        final Object[] args = { m };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("Augmentations Set cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void resolveAugmentationsTest() throws Exception {
        final Class[] parameterTypes = { Module.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("resolveAugmentations", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module m = mock(Module.class);

        final Set<AugmentationSchema> augmentations = new HashSet<>();

        final QName q1 = QName.create("q1", "2017-04-04", "q1");
        final QName q2 = QName.create("q2", "2017-04-04", "q2");
        final QName q3 = QName.create("q3", "2017-04-04", "q3");
        final QName q4 = QName.create("q4", "2017-04-04", "q4");
        final QName q5 = QName.create("q5", "2017-04-04", "q5");

        final AugmentationSchema augmentationSchema1 = mock(AugmentationSchema.class);
        when(augmentationSchema1.getTargetPath()).thenReturn(SchemaPath.create(true, q1, q2));
        final AugmentationSchema augmentationSchema2 = mock(AugmentationSchema.class);
        when(augmentationSchema2.getTargetPath()).thenReturn(SchemaPath.create(true, q3, q4, q5));
        augmentations.add(augmentationSchema1);
        augmentations.add(augmentationSchema2);

        when(m.getAugmentations()).thenReturn(augmentations);

        final Object[] args = { m };

        final List<AugmentationSchema> result =
                (List<AugmentationSchema>) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(result.get(0), augmentationSchema1);
        assertEquals(result.get(1), augmentationSchema2);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesNullPckgNameTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = null;
        final AugmentationSchema augmSchema = null;
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Package Name cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesNullAugSchemaTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";
        final AugmentationSchema augmSchema = null;
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Augmentation Schema cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesNullAugSchemaTargetPathTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";
        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        when(augmSchema.getTargetPath()).thenReturn(null);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("Augmentation Schema does not contain Target Path (Target Path is NULL).", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesNullAugSchemaTargetNodeTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";

        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DataSchemaNode schNode = null;
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("augment target not found: " + path, cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesNullAugTargetGTBTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";

        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof NullPointerException);
            assertEquals("Target type not yet generated: " + schNode, cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void augmentationToGenTypesAugUsesNullOrigTargetTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";

        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DerivableSchemaNode targetSchNode = mock(DerivableSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(targetSchNode.isAddedByUses()).thenReturn(true);
        final Optional optionalSchemaNode = Optional.absent();
        when(targetSchNode.getOriginal()).thenReturn(optionalSchemaNode);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("Failed to find target node from grouping in augmentation " + augmSchema + " in module "
                    + m.getName(), cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augmentationToGenTypesTargetChoicSchemaNodeTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";

        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final ChoiceSchemaNode targetSchNode = mock(ChoiceSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final TypeProvider typeProvider = null;

        final Map genCtx = mock(Map.class);
        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(augmPackName, "augm");
        mc.addChildNodeType(targetSchNode, gtb);
        moduleContexts.add(mc);
        when(genCtx.values()).thenReturn(moduleContexts);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        final Object[] args = { augmPackName, augmSchema, m, context, false, genCtx, genTypeBuilders, typeProvider };
        final Map result = (Map) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(result);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void augmentationToGenTypesTest() throws Exception {
        final Class[] parameterTypes = { String.class, AugmentationSchema.class, Module.class, SchemaContext.class,
                boolean.class, Map.class, Map.class, TypeProvider.class };
        final Method generate = AugmentToGenType.class.getDeclaredMethod("augmentationToGenTypes", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String augmPackName = "pckg.name";

        final AugmentationSchema augmSchema = mock(AugmentationSchema.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        when(augmSchema.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DerivableSchemaNode targetSchNode = mock(DerivableSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(targetSchNode.isAddedByUses()).thenReturn(true);
        final DataSchemaNode origSchNode = mock(DataSchemaNode.class);
        when(origSchNode.getPath()).thenReturn(path);
        when(origSchNode.isAddedByUses()).thenReturn(true);
        final Optional optionalSchemaNode = Optional.of(origSchNode);
        when(targetSchNode.getOriginal()).thenReturn(optionalSchemaNode);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final TypeProvider typeProvider = null;

        final Map<Module, ModuleContext> genCtx = new HashMap<>();

        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(augmPackName, "augm");
        mc.addChildNodeType(targetSchNode, gtb);
        moduleContexts.add(mc);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());

        final Object[] args =
                { augmPackName, augmSchema, moduleAug, context, false, genCtx, genTypeBuilders, typeProvider };
        final Map<Module, ModuleContext> result =
                (Map<Module, ModuleContext>) generate.invoke(AugmentToGenType.class, args);
        assertNotNull(result);
        final ModuleContext moduleContext = result.get(moduleAug);
        assertTrue(moduleContext.getAugmentations().get(0).getName().contains("Augm"));
        assertEquals("pckg.name", moduleContext.getAugmentations().get(0).getPackageName());
        assertTrue(moduleContext.getChildNode(path).getName().contains("Augm"));
        assertEquals("pckg.name", moduleContext.getChildNode(path).getPackageName());
    }

    @Test
    public void usesAugmentationToGenTypesNullPckgNameTest() throws Exception {
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, null, null, null, null, null, null, null, false, null);
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), "Package Name cannot be NULL.");
        }
    }

    @Test
    public void usesAugmentationToGenTypesNullAugSchemaNodeTest() throws Exception {
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, "", null, null, null, null, null, null, false, null);
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), "Augmentation Schema cannot be NULL.");
        }
    }

    @Test
    public void usesAugmentationToGenTypesNullAugSchemaNodeTargetPathTest() throws Exception {
        final AugmentationSchema augmentationSchema = mock(AugmentationSchema.class);
        when(augmentationSchema.getTargetPath()).thenReturn(null);
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, "", augmentationSchema, null, null, null, null, null,
                    false, null);
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof IllegalStateException);
            assertEquals(e.getMessage(), "Augmentation Schema does not contain Target Path (Target Path is NULL).");
        }
    }

    @Test
    public void usesAugmentationToGenTypesNullAugmentTargetTest() throws Exception {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchema augmentationSchema = mock(AugmentationSchema.class);
        when(augmentationSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmentationSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();


        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        try {
            AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm", augmentationSchema, moduleAug,
                    usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null);
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(e.getMessage(), "augment target not found: " + path);
        }
    }

    @Test
    public void usesAugmentationToGenTypesNullTargetGTBTest() throws Exception {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchema augmentationSchema = mock(AugmentationSchema.class);
        when(augmentationSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmentationSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        try {
            AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm", augmentationSchema, moduleAug,
                    usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null);
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof NullPointerException);
            assertEquals(e.getMessage(), "Target type not yet generated: " + schNode);
        }
    }

    @Test
    public void usesAugmentationToGenTypesTest() throws Exception {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchema augmentationSchema = mock(AugmentationSchema.class);
        when(augmentationSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmentationSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(schNode.getPath()).thenReturn(path);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("pckg.test.augm", "GtbAugm");
        mc.addChildNodeType(schNode, gtb);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        final Map<Module, ModuleContext> result = AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm",
                augmentationSchema, moduleAug, usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null);
        assertNotNull(result);
    }

    @Test
    public void usesAugmentationToGenTypesChoiceTest() throws Exception {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchema augmentationSchema = mock(AugmentationSchema.class);
        when(augmentationSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmentationSchema.getUses()).thenReturn(uses);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        final ChoiceSchemaNode schNode = mock(ChoiceSchemaNode.class);
        when(schNode.getPath()).thenReturn(path);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(moduleAug);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("pckg.test.augm", "GtbAugm");
        mc.addChildNodeType(schNode, gtb);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        final Map<Module, ModuleContext> result = AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm",
                augmentationSchema, moduleAug, usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null);
        assertNotNull(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void findOriginalTargetFromGroupingNonGroupingTest() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(schNode.getPath()).thenReturn(schemaPath);
        when(module.getDataChildByName(qnamePath)).thenReturn(schNode);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModuleByNamespaceAndRevision(qnamePath .getNamespace(), qnamePath.getRevision()))
                .thenReturn(module);
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = { context, schemaPath, usesNode };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Failed to generate code for augment in " + usesNode, cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void findOriginalTargetFromGroupingAsUsesFailedTest() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(schNode.getPath()).thenReturn(schemaPath);
        when(schNode.isAddedByUses()).thenReturn(true);
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        groupings.add(groupingDefinition);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(module.getGroupings()).thenReturn(groupings);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(module);
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = { context, schemaPath, usesNode };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
            assertEquals("Failed to generate code for augment in " + usesNode, cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void findOriginalTargetFromGroupingReturnNullTest() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final DataSchemaNode schNode = null;
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        groupings.add(groupingDefinition);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(module.getGroupings()).thenReturn(groupings);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(module);
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = { context, schemaPath, usesNode };
        final DataSchemaNode result = (DataSchemaNode) generate.invoke(AugmentToGenType.class, args);
        assertEquals(null, result);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void findOriginalTargetFromGroupingTest() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(schNode.getPath()).thenReturn(schemaPath);
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        groupings.add(groupingDefinition);
        final DerivableSchemaNode derivSchNode = mock(DerivableSchemaNode.class);
        when(derivSchNode.isAddedByUses()).thenReturn(true);
        final Optional optional = Optional.of(schNode);
        when(derivSchNode.getOriginal()).thenReturn(optional);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(derivSchNode);
        when(module.getGroupings()).thenReturn(groupings);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(module);
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = { context, schemaPath, usesNode };
        final DataSchemaNode result = (DataSchemaNode) generate.invoke(AugmentToGenType.class, args);
        assertEquals(schNode, result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void findOriginalTargetFromGroupingChoiceTest() throws Exception {
        final Class[] parameterTypes = { SchemaContext.class, SchemaPath.class, UsesNode.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("findOriginalTargetFromGrouping", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final ChoiceSchemaNode schNode = mock(ChoiceSchemaNode.class);
        when(schNode.getPath()).thenReturn(schemaPath);
        final Set<GroupingDefinition> groupings = new HashSet<>();
        final GroupingDefinition groupingDefinition = mock(GroupingDefinition.class);
        when(groupingDefinition.getQName()).thenReturn(qnamePath);
        groupings.add(groupingDefinition);
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(module.getGroupings()).thenReturn(groupings);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModuleByNamespaceAndRevision(qnamePath.getNamespace(), qnamePath.getRevision()))
                .thenReturn(module);
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = { context, schemaPath, usesNode };
        final DataSchemaNode result = (DataSchemaNode) generate.invoke(AugmentToGenType.class, args);
        assertEquals(schNode, result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void generateTypesFromAugmentedChoiceCasesNullPckgNameTest() throws Exception {
        final Class[] parameterTypes =
                { SchemaContext.class, Module.class, String.class, Type.class, ChoiceSchemaNode.class, Iterable.class,
                        DataNodeContainer.class, Map.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("generateTypesFromAugmentedChoiceCases", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = null;
        final Type targetType = null;
        final ChoiceSchemaNode targetNode = null;
        final Iterable augmentNodes = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        final Object[] args = { schemaContext, module, pckgName, targetType, targetNode, augmentNodes, usesNodeParent,
                genCtx, false, genTypeBuilder, null };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Base Package Name cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void generateTypesFromAugmentedChoiceCasesNullTargetType() throws Exception {
        final Class[] parameterTypes =
                { SchemaContext.class, Module.class, String.class, Type.class, ChoiceSchemaNode.class, Iterable.class,
                        DataNodeContainer.class, Map.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("generateTypesFromAugmentedChoiceCases", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = "";
        final Type targetType = null;
        final ChoiceSchemaNode targetNode = null;
        final Iterable augmentNodes = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        final Object[] args = { schemaContext, module, pckgName, targetType, targetNode, augmentNodes, usesNodeParent,
                genCtx, false, genTypeBuilder, null };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Referenced Choice Type cannot be NULL.", cause.getMessage());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void generateTypesFromAugmentedChoiceCasesNullAugmentNodes() throws Exception {
        final Class[] parameterTypes =
                { SchemaContext.class, Module.class, String.class, Type.class, ChoiceSchemaNode.class, Iterable.class,
                        DataNodeContainer.class, Map.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate =
                AugmentToGenType.class.getDeclaredMethod("generateTypesFromAugmentedChoiceCases", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = "";
        final Type targetType = mock(Type.class);
        final ChoiceSchemaNode targetNode = null;
        final Iterable augmentNodes = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        final Object[] args = { schemaContext, module, pckgName, targetType, targetNode, augmentNodes, usesNodeParent,
                genCtx, false, genTypeBuilder, null };
        try {
            generate.invoke(AugmentToGenType.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Set of Choice Case Nodes cannot be NULL.", cause.getMessage());
        }
    }

    // TODO test generateTypesFromAugmentedChoiceCases
}