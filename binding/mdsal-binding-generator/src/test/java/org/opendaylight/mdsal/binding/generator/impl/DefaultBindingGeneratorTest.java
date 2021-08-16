/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.yang.types.TypeProviderTest;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * General test suite revolving around {@link DefaultBindingGenerator}. This class holds tests originally aimed at
 * specific implementation methods, but now they really are all about integration testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultBindingGeneratorTest {
    public static EffectiveModelContext SCHEMA_CONTEXT;
    public static List<GeneratedType> TYPES;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResources(TypeProviderTest.class,
            "/base-yang-types.yang", "/test-type-provider-b.yang", "/test-type-provider.yang");
        TYPES = DefaultBindingGenerator.generateFor(SCHEMA_CONTEXT);
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
        TYPES = null;
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefToEnumTypeTest() {
        final Module module = Iterables.getOnlyElement(SCHEMA_CONTEXT.findModules("test-type-provider-b"));

        final QName leafNode = QName.create(module.getQNameModule(), "enum");
        final DataSchemaNode enumNode = module.findDataChildByName(leafNode).get();
        assertTrue(enumNode instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) enumNode;
        final TypeDefinition<?> leafType = leaf.getType();

//        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
//        assertNotNull(leafrefResolvedType1);

        final QName leafListNode = QName.create(module.getQNameModule(), "enums");
        final DataSchemaNode enumListNode = module.findDataChildByName(leafListNode).get();
        assertTrue(enumListNode instanceof LeafListSchemaNode);
        final LeafListSchemaNode leafList = (LeafListSchemaNode) enumListNode;
        final TypeDefinition<?> leafListType = leafList.getType();

//        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafListType, leafList);
//        assertNotNull(leafrefResolvedType2);
//        assertTrue(leafrefResolvedType2 instanceof ParameterizedType);
    }

//    private static void setReferencedTypeForTypeProvider(final AbstractTypeProvider provider) {
//        final LeafSchemaNode enumLeafNode = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo",
//            "resolve-direct-use-of-enum");
//        final TypeDefinition<?> enumLeafTypedef = enumLeafNode.getType();
//        provider.putReferencedType(enumLeafNode.getPath(),
//            Type.of(provider.javaTypeForSchemaDefinitionType(enumLeafTypedef, enumLeafNode)));
//
//        final LeafListSchemaNode enumListNode = provideLeafListNodeFromTopLevelContainer(TEST_TYPE_PROVIDER,
//            "foo", "list-of-enums");
//        final TypeDefinition<?> enumLeafListTypedef = enumListNode.getType();
//        provider.putReferencedType(enumListNode.getPath(),
//            Type.of(provider.javaTypeForSchemaDefinitionType(enumLeafListTypedef, enumListNode)));
//    }
}
