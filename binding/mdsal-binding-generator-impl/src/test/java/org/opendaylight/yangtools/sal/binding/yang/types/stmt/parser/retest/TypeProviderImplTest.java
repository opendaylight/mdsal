/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public class TypeProviderImplTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test (expected = YangValidationException.class)
    public void testLeafRefRelativeSelfReference() throws Exception {
        File relative = new File(getClass().getResource("/leafref/leafref-relative-invalid.yang").toURI());

        final SchemaContext schemaContext = RetestUtils.parseYangSources(relative);
        final Module moduleRelative = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lrr")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName("neighbor")).getDataChildByName("neighbor-id");
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test (expected = YangValidationException.class)
    public void testLeafRefAbsoluteSelfReference() throws Exception {
        File relative = new File(getClass().getResource("/leafref/leafref-absolute-invalid.yang").toURI());

        final SchemaContext schemaContext = RetestUtils.parseYangSources(relative);
        final Module moduleRelative = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lra")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName("neighbor")).getDataChildByName("neighbor-id");
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteValidReference() throws URISyntaxException, SourceException, FileNotFoundException, ReactorException {
        File valid = new File(getClass().getResource("/leafref/leafref-valid.yang").toURI());

        final SchemaContext schemaContext = RetestUtils.parseYangSources(valid);
        final Module moduleValid = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lrv")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName("neighbor")).getDataChildByName
                ("neighbor-id");
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        Type leafrefRelResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel);
        assertNotNull(leafrefRelResolvedType);

        DataSchemaNode leafrefAbs = ((ListSchemaNode) moduleValid.getDataChildByName("neighbor")).getDataChildByName
                ("neighbor2-id");
        LeafSchemaNode leafAbs = (LeafSchemaNode) leafrefAbs;
        TypeDefinition<?> leafTypeAbs = leafAbs.getType();
        Type leafrefAbsResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeAbs, leafAbs);
        assertNotNull(leafrefAbsResolvedType);
    }

    @Test
    public void testMethodsOfTypeProviderImpl() throws URISyntaxException, SourceException, FileNotFoundException, ReactorException {
        final File abstractTopology = new File(getClass().getResource("/base-yang-types.yang")
                .toURI());

        final SchemaContext schemaContext = RetestUtils.parseYangSources(abstractTopology);

        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        final SchemaPath refTypePath = SchemaPath.create(true, QName.create("cont1"), QName.create("list1"));
        final GeneratedTypeBuilderImpl refType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        typeProvider.putReferencedType(refTypePath, refType);
        final StringTypeDefinition stringType = BaseTypes.stringType();
        LeafSchemaNodeBuilder leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(stringType);
        LeafSchemaNode leafSchemaNode = leafSchemaNodeBuilder.build();

        // test constructor
        assertNotNull(typeProvider);

        // test getAdditionalTypes() method
        assertFalse(typeProvider.getAdditionalTypes().isEmpty());

        // test getConstructorPropertyName() method
        assertTrue(typeProvider.getConstructorPropertyName(null).isEmpty());
        assertEquals("value", typeProvider.getConstructorPropertyName(stringType));

        // test getParamNameFromType() method
        assertEquals("string", typeProvider.getParamNameFromType(stringType));

        // test getTypeDefaultConstruction() method for string type
        assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));

        // binary type
        final BinaryTypeDefinition binaryType = BaseTypes.binaryType();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(binaryType);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("new byte[] {-45}", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "01"));

        // boolean type
        final BooleanTypeDefinition booleanType = BaseTypes.booleanType();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(booleanType);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("new java.lang.Boolean(\"false\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "false"));

        // decimal type
        final DecimalTypeDefinition decimalType = BaseTypes.decimalTypeBuilder(refTypePath).setFractionDigits(4).build();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(decimalType);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("new java.math.BigDecimal(\"111\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "111"));

        // empty type
        final EmptyTypeDefinition emptyType = BaseTypes.emptyType();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(emptyType);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("new java.lang.Boolean(\"default value\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));

        // enum type
        expException.expect(NoSuchElementException.class);
        final EnumTypeDefinition enumType =  BaseTypes.enumerationTypeBuilder(refTypePath).build();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(enumType);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));

        // identityref type
        expException.expect(UnsupportedOperationException.class);
        expException.expectMessage("Cannot get default construction for identityref type");

        final ModuleBuilder testModBuilder = new ModuleBuilder("test-module", "/test");
        final IdentitySchemaNodeBuilder identityNodeBuilder = testModBuilder.addIdentity(QName.create("IdentityRefTest"), 111, SchemaPath.ROOT);
        final IdentitySchemaNode identitySchemaNode = identityNodeBuilder.build();

        final IdentityrefTypeBuilder identityRefBuilder = BaseTypes.identityrefTypeBuilder(refTypePath);
        identityRefBuilder.setIdentity(identitySchemaNode);
        final IdentityrefTypeDefinition identityRef =  identityRefBuilder.build();
        leafSchemaNodeBuilder = new LeafSchemaNodeBuilder("test-module", 111, QName.create("Cont1"), SchemaPath.ROOT);
        leafSchemaNodeBuilder.setType(identityRef);

        leafSchemaNodeBuilder.setParent(identityNodeBuilder);
        leafSchemaNode = leafSchemaNodeBuilder.build();
        assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));
    }
}
