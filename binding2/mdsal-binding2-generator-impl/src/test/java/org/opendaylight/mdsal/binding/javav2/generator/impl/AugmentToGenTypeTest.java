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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
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

    @Test(expected = UnsupportedOperationException.class)
    public void constructorTest() throws Throwable {
        final Constructor<AugmentToGenType> constructor = AugmentToGenType.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final Object[] objs = {};
        try {
            constructor.newInstance(objs);
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void generateNullModuleTest() {
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        try {
            AugmentToGenType.generate(m, context, typeProvider, genCtx, genTypeBuilders, false);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Module reference cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateNullModuleNameTest() {
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = mock(Module.class);
        when(m.getName()).thenReturn(null);

        try {
            AugmentToGenType.generate(m, context, typeProvider, genCtx, genTypeBuilders, false);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Module name cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateNullModuleAugmentationsTest() {
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("name");
        when(m.getAugmentations()).thenReturn(null);

        try {
            AugmentToGenType.generate(m, context, typeProvider, genCtx, genTypeBuilders, false);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Augmentations Set cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateWithoutAugmentationsTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/generator/test.yang");
        final TypeProvider typeProvider = new TypeProviderImpl(context);
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Map<Module, ModuleContext> invoke = AugmentToGenType.generate(context.getModules().iterator().next(),
            context, typeProvider, genCtx, genTypeBuilders, false);
        assertNotNull(invoke);
    }

    @Test
    public void generateWithAugmentationsTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/generator/test-augment.yang");
        final TypeProvider typeProvider = new TypeProviderImpl(context);
        final Map<Module, ModuleContext> genCtx = mock(Map.class);
        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext moduleContext = new ModuleContext();
        moduleContexts.add(moduleContext);
        final QName create = QName.create("urn:test:simple:test", "2017-02-06", "my-cont");
        final SchemaNode schemaNode = mock(SchemaNode.class);
        when(schemaNode.getPath()).thenReturn(SchemaPath.create(true, create));
        moduleContext.addChildNodeType(schemaNode, new GeneratedTypeBuilderImpl("test", "Test", moduleContext));
        when(genCtx.values()).thenReturn(moduleContexts);
        when(genCtx.get(context.getModules().iterator().next())).thenReturn(moduleContext);
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Map<Module, ModuleContext> invoke = AugmentToGenType.generate(context.getModules().iterator().next(),
            context, typeProvider, genCtx, genTypeBuilders, false);
        assertNotNull(invoke);
    }

    @Test
    public void resolveAugmentationsNullModuleTest() {
        try {
            AugmentToGenType.resolveAugmentations(null, null);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Module reference cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void resolveAugmentationsNullAugmentationsTest() {
        final Module m = mock(Module.class);
        when(m.getAugmentations()).thenReturn(null);
        final SchemaContext schemaContext = mock(SchemaContext.class);

        try {
            AugmentToGenType.resolveAugmentations(m, schemaContext);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Augmentations Set cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void resolveAugmentationsTest() {
        final Module m = mock(Module.class);
        final Module m2 = mock(Module.class);
        final SchemaContext schemaContext = mock(SchemaContext.class);

        final Set<AugmentationSchemaNode> augmentations = new HashSet<>();

        final QName q1 = QName.create("q1", "2017-04-04", "q1");
        final QName q2 = QName.create("q2", "2017-04-04", "q2");
        final QName q3 = QName.create("q3", "2017-04-04", "q3");
        final QName q4 = QName.create("q4", "2017-04-04", "q4");
        final QName q5 = QName.create("q5", "2017-04-04", "q5");

        final AugmentationSchemaNode AugmentationSchemaNode1 = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode1.getTargetPath()).thenReturn(SchemaPath.create(true, q1, q2));
        final AugmentationSchemaNode AugmentationSchemaNode2 = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode2.getTargetPath()).thenReturn(SchemaPath.create(true, q3, q4, q5));
        augmentations.add(AugmentationSchemaNode1);
        augmentations.add(AugmentationSchemaNode2);

        when(m.getAugmentations()).thenReturn(augmentations);
        when(schemaContext.findModule(q1.getModule())).thenReturn(Optional.of(m2));
        when(schemaContext.findModule(q3.getModule())).thenReturn(Optional.of(m2));

        final List<AugmentationSchemaNode> result = AugmentToGenType.resolveAugmentations(m, schemaContext);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(result.get(0), AugmentationSchemaNode1);
        assertEquals(result.get(1), AugmentationSchemaNode2);
    }

    @Test
    public void augmentationToGenTypesNullPckgNameTest() {
        final String augmPackName = null;
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = null;
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Package Name cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesNullAugSchemaTest() {
        final String augmPackName = "pckg.name";
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = null;
        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Augmentation List Entry cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesNullAugSchemaTargetPathTest() {
        final String augmPackName = "pckg.name";
        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        when(augmSchema.getTargetPath()).thenReturn(null);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(null);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Augmentation List Entry does not contain Target Path (Target Path is NULL).", e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesNullAugSchemaListTest() {
        final String augmPackName = "pckg.name";
        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = null;
        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final Module m = null;

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Augmentation List cannot be empty.", e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesNullAugSchemaTargetNodeTest() {
        final String augmPackName = "pckg.name";

        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(augmSchema);
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DataSchemaNode schNode = null;
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("augment target not found: " + path, e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesNullAugTargetGTBTest() {
        final String augmPackName = "pckg.name";

        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(augmSchema);
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(schNode);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final NullPointerException e) {
            assertEquals("Target type not yet generated: " + schNode, e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesAugUsesNullOrigTargetTest() {
        final String augmPackName = "pckg.name";

        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(augmSchema);
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DerivableSchemaNode targetSchNode = mock(DerivableSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(targetSchNode.isAddedByUses()).thenReturn(true);
        when(targetSchNode.getOriginal()).thenReturn(Optional.empty());
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final TypeProvider typeProvider = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        try {
            AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, m, context, false, genCtx,
                genTypeBuilders, typeProvider);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Failed to find target node from grouping in augmentation " + augmSchema + " in module "
                    + m.getName(), e.getMessage());
        }
    }

    @Test
    public void augmentationToGenTypesTargetChoicSchemaNodeTest() {
        final String augmPackName = "pckg.name";

        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(augmSchema);
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final ChoiceSchemaNode targetSchNode = mock(ChoiceSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final TypeProvider typeProvider = null;

        final Map<Module, ModuleContext> genCtx = mock(Map.class);
        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(augmPackName, "augm", mc);
        mc.addChildNodeType(targetSchNode, gtb);
        moduleContexts.add(mc);
        when(genCtx.values()).thenReturn(moduleContexts);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final Module m = mock(Module.class);
        when(m.getName()).thenReturn("augm-module");
        when(m.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(m.getRevision()).thenReturn(qnamePath.getRevision());

        final Map<Module, ModuleContext> result = AugmentToGenType.augmentationToGenTypes(augmPackName,
            schemaPathAugmentListEntry, m, context, false, genCtx, genTypeBuilders, typeProvider);
        assertNotNull(result);
    }

    @Test
    public void augmentationToGenTypesTest() {
        final String augmPackName = "pckg.name";

        final AugmentationSchemaNode augmSchema = mock(AugmentationSchemaNode.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        when(augmSchema.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(augmSchema.getUses()).thenReturn(uses);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        when(augmSchema.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(augmSchema);
        final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry = mock(Map.Entry.class);
        when(schemaPathAugmentListEntry.getKey()).thenReturn(path);
        when(schemaPathAugmentListEntry.getValue()).thenReturn(AugmentationSchemaNodeList);

        final SchemaContext context = mock(SchemaContext.class);
        final Module moduleAug = mock(Module.class);
        final DerivableSchemaNode targetSchNode = mock(DerivableSchemaNode.class);
        when(targetSchNode.getPath()).thenReturn(path);
        when(targetSchNode.isAddedByUses()).thenReturn(true);
        final DataSchemaNode origSchNode = mock(DataSchemaNode.class);
        when(origSchNode.getPath()).thenReturn(path);
        when(origSchNode.isAddedByUses()).thenReturn(true);
        when(origSchNode.getQName()).thenReturn(QName.create("test", "2017-04-04", "aug-node"));
        final Optional optionalSchemaNode = Optional.of(origSchNode);
        when(targetSchNode.getOriginal()).thenReturn(optionalSchemaNode);
        when(moduleAug.getDataChildByName(qnamePath)).thenReturn(targetSchNode);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final TypeProvider typeProvider = null;

        final Map<Module, ModuleContext> genCtx = new HashMap<>();

        final Collection<ModuleContext> moduleContexts = new ArrayList<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(augmPackName, "augm", mc);
        mc.addChildNodeType(targetSchNode, gtb);
        moduleContexts.add(mc);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        when(moduleAug.getName()).thenReturn("augm-module");
        when(moduleAug.getNamespace()).thenReturn(qnamePath.getNamespace());
        when(moduleAug.getRevision()).thenReturn(qnamePath.getRevision());

        final Map<Module, ModuleContext> result =
                AugmentToGenType.augmentationToGenTypes(augmPackName, schemaPathAugmentListEntry, moduleAug, context,
                    false, genCtx, genTypeBuilders, typeProvider);
        assertNotNull(result);
        final ModuleContext moduleContext = result.get(moduleAug);
        assertTrue(moduleContext.getAugmentations().get(0).getName().contains("Augm"));
        assertEquals("pckg.name.data", moduleContext.getAugmentations().get(0).getPackageName());
        assertTrue(moduleContext.getChildNode(path).getName().contains("Augm"));
        assertEquals("pckg.name", moduleContext.getChildNode(path).getPackageName());
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesNullPckgNameTest() {
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, null, null, null, null, null, null, null, false, null, null);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Package Name cannot be NULL.");
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesNullAugSchemaListEntryTest() {
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, "", null, null, null, null, null, null, false, null, null);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Augmentation Schema List Entry cannot be NULL.");
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesEmptyAugSchemaListTest() {
        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, "", AugmentationSchemaNodeList, null, null, null, null, null,
                    false, null, null);
        } catch (final IllegalStateException e) {
            assertEquals(e.getMessage(), "Augmentation Schema List cannot be empty");
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesNullAugSchemaNodeTargetPathTest() {
        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getTargetPath()).thenReturn(null);
        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(AugmentationSchemaNode);
        try {
            AugmentToGenType.usesAugmentationToGenTypes(null, "", AugmentationSchemaNodeList, null, null, null, null, null,
                    false, null, null);
        } catch (final IllegalStateException e) {
            assertEquals(e.getMessage(), "Augmentation Schema does not contain Target Path (Target Path is NULL).");
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesNullAugmentTargetTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(AugmentationSchemaNode.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(AugmentationSchemaNode);

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

        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();


        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        try {
            AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm", AugmentationSchemaNodeList, moduleAug,
                    usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null, null);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "augment target not found: " + path);
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesNullTargetGTBTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(AugmentationSchemaNode.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(AugmentationSchemaNode);

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

        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        try {
            AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm", AugmentationSchemaNodeList, moduleAug,
                    usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null, null);
        } catch (final NullPointerException e) {
            assertEquals(e.getMessage(), "Target type not yet generated: " + schNode);
        }
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(AugmentationSchemaNode.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(AugmentationSchemaNode);

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
        when(schNode.getQName()).thenReturn(QName.create("test", "2017-04-04", "aug-node"));
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("pckg.test.augm", "GtbAugm", mc);
        mc.addChildNodeType(schNode, gtb);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        final Map<Module, ModuleContext> result = AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm",
                AugmentationSchemaNodeList, moduleAug, usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null,
                BindingNamespaceType.Data);
        assertNotNull(result);
    }

    @Deprecated
    @Test
    public void usesAugmentationToGenTypesChoiceTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "aug");
        final SchemaPath path = SchemaPath.create(true, qnamePath);
        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getTargetPath()).thenReturn(path);
        final Set<UsesNode> uses = new HashSet<>();
        when(AugmentationSchemaNode.getUses()).thenReturn(uses);

        final List<AugmentationSchemaNode> AugmentationSchemaNodeList = new ArrayList<>();
        AugmentationSchemaNodeList.add(AugmentationSchemaNode);

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
        when(schNode.getQName()).thenReturn(QName.create("test", "2017-04-04", "aug-node"));
        when(groupingDefinition.getDataChildByName(qnamePath)).thenReturn(schNode);
        groupings.add(groupingDefinition);
        when(moduleAug.getGroupings()).thenReturn(groupings);

        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(moduleAug));

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext mc = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("pckg.test.augm", "GtbAugm", mc);
        mc.addChildNodeType(schNode, gtb);
        genCtx.put(moduleAug, mc);

        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

        final UsesNode usesNode = mock(UsesNode.class);
        final DataNodeContainer usesNodeParent = mock(DataNodeContainer.class);

        when(usesNode.getGroupingPath()).thenReturn(path);

        final Map<Module, ModuleContext> result = AugmentToGenType.usesAugmentationToGenTypes(context, "pckg.test.augm",
                AugmentationSchemaNodeList, moduleAug, usesNode, usesNodeParent, genCtx, genTypeBuilders, false, null,
                BindingNamespaceType.Data);
        assertNotNull(result);
    }

    @Test
    public void findOriginalTargetFromGroupingNonGroupingTest() {
        final Module module = mock(Module.class);
        final QName qnamePath = QName.create("test", "2017-04-04", "test");
        final SchemaPath schemaPath = SchemaPath.create(true, qnamePath);
        final DataSchemaNode schNode = mock(DataSchemaNode.class);
        when(schNode.getPath()).thenReturn(schemaPath);
        when(module.getDataChildByName(qnamePath)).thenReturn(schNode);

        final SchemaContext context = mock(SchemaContext.class);
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        try {
            AugmentToGenType.findOriginalTargetFromGrouping(context, schemaPath, usesNode);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Failed to generate code for augment in " + usesNode, e.getMessage());
        }
    }

    @Test
    public void findOriginalTargetFromGroupingAsUsesFailedTest() {
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
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = {  };
        try {
            AugmentToGenType.findOriginalTargetFromGrouping(context, schemaPath, usesNode);
            fail();
        } catch (final IllegalStateException e) {
            assertEquals("Failed to generate code for augment in " + usesNode, e.getMessage());
        }
    }

    @Test
    public void findOriginalTargetFromGroupingReturnNullTest() {
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
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final Object[] args = {  };
        final DataSchemaNode result = AugmentToGenType.findOriginalTargetFromGrouping(context, schemaPath, usesNode);
        assertEquals(null, result);
    }

    @Test
    public void findOriginalTargetFromGroupingTest() {
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
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final DataSchemaNode result = AugmentToGenType.findOriginalTargetFromGrouping(context, schemaPath, usesNode);
        assertEquals(schNode, result);
    }

    @Test
    public void findOriginalTargetFromGroupingChoiceTest() {
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
        when(context.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));
        final UsesNode usesNode = mock(UsesNode.class);
        when(usesNode.getGroupingPath()).thenReturn(schemaPath);

        final DataSchemaNode result = AugmentToGenType.findOriginalTargetFromGrouping(context, schemaPath, usesNode);
        assertEquals(schNode, result);
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesNullPckgNameTest() {
        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = null;
        final Type targetType = null;
        final ChoiceSchemaNode targetNode = null;
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        try {
            AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext, module, pckgName, targetType,
                targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false, genTypeBuilder, null,
                BindingNamespaceType.Data);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Base Package Name cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesNullTargetType() {
        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = "";
        final Type targetType = null;
        final ChoiceSchemaNode targetNode = null;
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        try {
            AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext, module, pckgName, targetType,
                targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false, genTypeBuilder, null,
                BindingNamespaceType.Data);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Referenced Choice Type cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesNullAugmentNodes() {
        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = "";
        final Type targetType = mock(Type.class);
        final ChoiceSchemaNode targetNode = null;
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = null;
        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        final Object[] args = {  };
        try {
            AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext, module, pckgName, targetType,
                targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false, genTypeBuilder, null,
                BindingNamespaceType.Data);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Set of Choice Case Nodes cannot be NULL.", e.getMessage());
        }
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesNullCaseNodeTest() {
        final SchemaContext schemaContext = null;
        final Module module = null;
        final String pckgName = "";
        final Type targetType = mock(Type.class);
        final ChoiceSchemaNode targetNode = null;
        final Set<DataSchemaNode> augmentNodes = new HashSet<>();
        final DataSchemaNode caseNode = null;
        augmentNodes.add(caseNode);

        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getChildNodes()).thenReturn(augmentNodes);
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = new ArrayList<>();
        schemaPathAugmentListEntry.add(AugmentationSchemaNode);

        final DataNodeContainer usesNodeParent = null;
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        final Map<Module, ModuleContext> result = AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext,
            module, pckgName, targetType, targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false,
            genTypeBuilder, null, BindingNamespaceType.Data);
        assertEquals(genCtx, result);
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesNullChildTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "chcase");
        final QName qnamePath2 = QName.create("test", "2017-04-04", "chcase2");
        final SchemaPath path = SchemaPath.create(true, qnamePath, qnamePath2);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("test-module-case");
        final DataSchemaNode schemaNode = mock(DataSchemaNode.class);
        when(module.getDataChildByName(qnamePath)).thenReturn(schemaNode);
        when(module.getRevision()).thenReturn(qnamePath.getRevision());
        when(module.getNamespace()).thenReturn(qnamePath.getNamespace());
        final String pckgName = "test.augment.choice.cases";
        final Type targetType = mock(Type.class);
        when(targetType.getFullyQualifiedName()).thenReturn(Augmentable.class.getName());
        final Set<DataSchemaNode> augmentNodes = new HashSet<>();
        final ChoiceCaseNode caseNode = mock(ChoiceCaseNode.class);
        when(caseNode.getPath()).thenReturn(path);
        when(caseNode.getQName()).thenReturn(qnamePath);
        when(caseNode.getDescription()).thenReturn(Optional.empty());
        when(caseNode.getReference()).thenReturn(Optional.empty());

        augmentNodes.add(caseNode);

        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getChildNodes()).thenReturn(augmentNodes);
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = new ArrayList<>();
        schemaPathAugmentListEntry.add(AugmentationSchemaNode);

        final DataNodeContainer usesNodeParent = null;
        final ChoiceSchemaNode targetNode = mock(ChoiceSchemaNode.class);
        when(targetNode.getPath()).thenReturn(path);
        when(targetNode.getDescription()).thenReturn(Optional.empty());
        when(targetNode.getReference()).thenReturn(Optional.empty());

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        genCtx.put(module, new ModuleContext());
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(pckgName, "test-case-node-augment", genCtx.get(module));
        genCtx.get(module).addCaseType(path, gtb);
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        when(schemaContext.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));

        try {
            AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext, module, pckgName, targetType,
                targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false, genTypeBuilder, null,
                BindingNamespaceType.Data);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals("Failed to find parent type of choice " + targetNode, e.getMessage());
        }
    }

    @Test
    public void generateTypesFromAugmentedChoiceCasesTest() {
        final QName qnamePath = QName.create("test", "2017-04-04", "chcase");
        final QName qnamePath2 = QName.create("test", "2017-04-04", "chcase2");
        final SchemaPath path = SchemaPath.create(true, qnamePath, qnamePath2);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("test-module-case");
        final ChoiceCaseNode schemaNode = mock(ChoiceCaseNode.class);
        when(schemaNode.getPath()).thenReturn(path);
        when(module.getDataChildByName(qnamePath)).thenReturn(schemaNode);
        when(module.getRevision()).thenReturn(qnamePath.getRevision());
        when(module.getNamespace()).thenReturn(qnamePath.getNamespace());
        final String pckgName = "test.augment.choice.cases";
        final Type targetType = mock(Type.class);
        when(targetType.getFullyQualifiedName()).thenReturn(Augmentable.class.getName());
        final Set<DataSchemaNode> augmentNodes = new HashSet<>();
        final ChoiceCaseNode caseNode = mock(ChoiceCaseNode.class);
        when(caseNode.getPath()).thenReturn(path);
        when(caseNode.getQName()).thenReturn(qnamePath);
        when(caseNode.getDescription()).thenReturn(Optional.empty());
        when(caseNode.getReference()).thenReturn(Optional.empty());
        augmentNodes.add(caseNode);

        final AugmentationSchemaNode AugmentationSchemaNode = mock(AugmentationSchemaNode.class);
        when(AugmentationSchemaNode.getChildNodes()).thenReturn(augmentNodes);
        final List<AugmentationSchemaNode> schemaPathAugmentListEntry = new ArrayList<>();
        schemaPathAugmentListEntry.add(AugmentationSchemaNode);

        final DataNodeContainer usesNodeParent = null;
        final ChoiceSchemaNode targetNode = mock(ChoiceSchemaNode.class);
        when(targetNode.getPath()).thenReturn(path);
        when(targetNode.getDescription()).thenReturn(Optional.empty());
        when(targetNode.getReference()).thenReturn(Optional.empty());
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext moduleContext = new ModuleContext();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl(pckgName, "test-case-node-augment", moduleContext);
        moduleContext.addCaseType(path, gtb);
        genCtx.put(module, moduleContext);
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilder = new HashMap<>();

        when(schemaContext.findModule(qnamePath.getModule())).thenReturn(Optional.of(module));

        final Map<Module, ModuleContext> result = AugmentToGenType.generateTypesFromAugmentedChoiceCases(schemaContext,
            module, pckgName, targetType, targetNode, schemaPathAugmentListEntry, usesNodeParent, genCtx, false,
            genTypeBuilder, null, BindingNamespaceType.Data);
        assertNotNull(result);
        assertEquals(result.get(module), moduleContext);
    }
}
