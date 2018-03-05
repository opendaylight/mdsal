/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.binding.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Test suite for testing public methods in TypeProviderImpl class
 *
 * @see org.opendaylight.mdsal.binding.yang.types.TypeProviderImpl
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
@RunWith(JUnit4.class)
public class TypeProviderTest {

    private SchemaContext schemaContext;
    private Set<Module> schemaModules;
    private Module testTypeProviderModule;

    @Mock
    private SchemaPath schemaPath;

    @Mock
    private SchemaNode schemaNode;

    @Before
    public void setUp() throws SourceException, ReactorException {
        MockitoAnnotations.initMocks(this);
        this.schemaContext = TypeProviderModel.createTestContext();
        assertNotNull(this.schemaContext);
        this.schemaModules = this.schemaContext.getModules();
        assertNotNull(this.schemaModules);
        this.testTypeProviderModule = resolveModule(TypeProviderModel.TEST_TYPE_PROVIDER_MODULE_NAME);
        assertNotNull(this.testTypeProviderModule);
    }

    private Module resolveModule(final String moduleName) {
        assertNotNull(moduleName);
        for (final Module m : this.schemaModules) {
            if (moduleName.equals(m.getName())) {
                return m;
            }
        }
        assertTrue("Unable to resolve module " + moduleName + ". No module present within Schema Context!" , false);
        return null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void typeProviderInstanceWithNullSchemaContextTest() {
        new RuntimeTypeProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putReferencedTypeWithNullSchemaPathParamTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        provider.putReferencedType(null, null);
        provider.putReferencedType(this.schemaPath, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putReferencedTypeWithNullRefTypeParamTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        provider.putReferencedType(this.schemaPath, null);
    }

    @Test
    public void getAdditionalTypesTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        assertNotNull(provider.getAdditionalTypes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionTypeNullTypedefTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        provider.javaTypeForSchemaDefinitionType(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionTypeTypedefNullQNameTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final TestIntegerTypeDefinition testTypedef = new TestIntegerTypeDefinition();
        provider.javaTypeForSchemaDefinitionType(testTypedef, null, null);
    }

    private static LeafSchemaNode provideLeafNodeFromTopLevelContainer(final Module module, final String containerName, final String leafNodeName) {
        final QName containerNode = QName.create(module.getQNameModule(), containerName);
        final DataSchemaNode rootNode = module.getDataChildByName(containerNode);
        assertNotNull("Container foo is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof DataNodeContainer);

        final QName leafNode = QName.create(module.getQNameModule(), leafNodeName);
        final DataNodeContainer rootContainer = (DataNodeContainer) rootNode;
        final DataSchemaNode node = rootContainer.getDataChildByName(leafNode);
        assertNotNull(node);
        assertTrue(node instanceof LeafSchemaNode);
        return (LeafSchemaNode) node;
    }

    private static LeafListSchemaNode provideLeafListNodeFromTopLevelContainer(final Module module, final String containerName, final String leafListNodeName) {
        final QName containerNode = QName.create(module.getQNameModule(), containerName);
        final DataSchemaNode rootNode = module.getDataChildByName(containerNode);
        assertNotNull("Container foo is not present in root of module " + module.getName(), rootNode);
        assertTrue(rootNode instanceof DataNodeContainer);

        final DataNodeContainer rootContainer = (DataNodeContainer) rootNode;
        final QName leafListNode = QName.create(module.getQNameModule(), leafListNodeName);
        final DataSchemaNode node = rootContainer.getDataChildByName(leafListNode);
        assertNotNull(node);
        assertTrue(node instanceof LeafListSchemaNode);
        return (LeafListSchemaNode) node;
    }

    @Test
    public void javaTypeForSchemaDefinitionExtTypeTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "yang-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("base-yang-types", genTO.getModuleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914", genTO.getPackageName());
        assertEquals("YangInt8", genTO.getName());
        assertEquals(1, genTO.getProperties().size());
    }

    @Test
    public void javaTypeForSchemaDefinitionRestrictedExtTypeTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "restricted-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914", genTO.getPackageName());
        assertEquals("YangInt8Restricted", genTO.getName());
        assertEquals(1, genTO.getProperties().size());
        final Optional<? extends RangeConstraint<?>> rangeConstraints = genTO.getRestrictions().getRangeConstraint();

        assertTrue(rangeConstraints.isPresent());
        final Range<?> constraint = rangeConstraints.get().getAllowedRanges().asRanges().iterator().next();
        assertEquals((byte) 1, constraint.lowerEndpoint());
        assertEquals((byte) 100, constraint.upperEndpoint());
    }

    @Test
    public void javaTypeForSchemaDefinitionEmptyStringPatternTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module testTypeProvider = resolveModule("test-type-provider");
        final TypeDefinition<?> emptyPatternString = resolveTypeDefinitionFromModule(testTypeProvider, "empty-pattern-string");

        assertNotNull(emptyPatternString);
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(emptyPatternString);

        Type result = provider.javaTypeForSchemaDefinitionType(emptyPatternString, emptyPatternString, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        result = provider.generatedTypeForExtendedDefinitionType(emptyPatternString, emptyPatternString);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
    }

    private static TypeDefinition<?> resolveTypeDefinitionFromModule(final Module module, final String typedefName) {
        TypeDefinition<?> result = null;
        final Set<TypeDefinition<?>> typeDefs = module.getTypeDefinitions();
        for (final TypeDefinition<?> typedef : typeDefs) {
            if (typedef.getQName().getLocalName().equals(typedefName)) {
                result = typedef;
            }
        }
        return result;
    }

    /**
     * FIXME: Remove @Ignore annotation once the bug https://bugs.opendaylight.org/show_bug.cgi?id=1862 is fixed
     */
    @Ignore
    @Test
    public void bug1862RestrictedTypedefTransformationTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo",
            "bug-1862-restricted-typedef");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        //TODO: complete test after bug 1862 is fixed
    }

    @Test
    public void javaTypeForSchemaDefinitionEnumExtTypeTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo",
            "resolve-enum-leaf");
        TypeDefinition<?> leafType = leaf.getType();
        Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof Enumeration);

        final Enumeration enumType = (Enumeration) result;
        final List<Enumeration.Pair> enumValues = enumType.getValues();
        assertTrue(!enumValues.isEmpty());
        assertEquals("a", enumValues.get(0).getName());
        assertEquals("b", enumValues.get(1).getName());
        assertEquals("A", enumValues.get(0).getMappedName());
        assertEquals("B", enumValues.get(1).getMappedName());

        leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "resolve-direct-use-of-enum");
        leafType = leaf.getType();
        result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ConcreteType);

        assertEquals("java.lang", result.getPackageName());
        assertEquals("Enum", result.getName());
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefExtTypeTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "bar", "leafref-value");
        TypeDefinition<?> leafType = leaf.getType();
        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);
        assertTrue(leafrefResolvedType1 instanceof GeneratedTransferObject);

        final Module module = resolveModule("test-type-provider-b");
        final QName leafNode = QName.create(module.getQNameModule(), "id");
        final DataSchemaNode rootNode = module.getDataChildByName(leafNode);
        assertNotNull("leaf id is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof LeafSchemaNode);
        leaf = (LeafSchemaNode) rootNode;
        leafType = leaf.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof GeneratedTransferObject);
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefToEnumTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(this.schemaContext);

        setReferencedTypeForTypeProvider(provider);

        final Module module = resolveModule("test-type-provider-b");

        final QName leafNode = QName.create(module.getQNameModule(), "enum");
        final DataSchemaNode enumNode = module.getDataChildByName(leafNode);
        assertNotNull("leaf enum is not present in root of module " + module.getName(), enumNode);
        assertTrue(enumNode instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) enumNode;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);
        assertTrue(leafrefResolvedType1 instanceof ReferencedTypeImpl);

        final QName leafListNode = QName.create(module.getQNameModule(), "enums");
        final DataSchemaNode enumListNode = module.getDataChildByName(leafListNode);
        assertNotNull("leaf-list enums is not present in root of module " + module.getName(), enumNode);
        assertTrue(enumListNode instanceof LeafListSchemaNode);
        final LeafListSchemaNode leafList = (LeafListSchemaNode) enumListNode;
        final TypeDefinition<?> leafListType = leafList.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafListType, leafList);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof ParameterizedType);
    }

    private void setReferencedTypeForTypeProvider(final AbstractTypeProvider provider) {
        final LeafSchemaNode enumLeafNode = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo",
            "resolve-direct-use-of-enum");
        final TypeDefinition<?> enumLeafTypedef = enumLeafNode.getType();
        Type enumType = provider.javaTypeForSchemaDefinitionType(enumLeafTypedef, enumLeafNode);

        Type refType = new ReferencedTypeImpl(enumType.getPackageName(), enumType.getName());
        provider.putReferencedType(enumLeafNode.getPath(), refType);

        final LeafListSchemaNode enumListNode = provideLeafListNodeFromTopLevelContainer(this.testTypeProviderModule,
            "foo", "list-of-enums");
        final TypeDefinition<?> enumLeafListTypedef = enumListNode.getType();
        enumType = provider.javaTypeForSchemaDefinitionType(enumLeafListTypedef, enumListNode);

        refType = new ReferencedTypeImpl(enumType.getPackageName(), enumType.getPackageName());
        provider.putReferencedType(enumListNode.getPath(), refType);
    }

    @Test
    public void javaTypeForSchemaDefinitionConditionalLeafrefTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        final Module module = resolveModule("test-type-provider-b");

        final QName leafrefNode = QName.create(module.getQNameModule(), "conditional-leafref");
        final DataSchemaNode condLeaf = module.getDataChildByName(leafrefNode);
        assertNotNull("leaf conditional-leafref is not present in root of module "+ module.getName(), condLeaf);
        assertTrue(condLeaf instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type resultType = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(resultType);
        assertTrue(resultType instanceof ConcreteType);
        assertEquals("java.lang", resultType.getPackageName());
        assertEquals("Object", resultType.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionInvalidLeafrefPathTest() {
        final TypeProvider provider = new CodegenTypeProvider(this.schemaContext);
        final Module module = resolveModule("test-type-provider-b");

        final QName leafrefNode = QName.create(module.getQNameModule(), "unreslovable-leafref");
        final DataSchemaNode condLeaf = module.getDataChildByName(leafrefNode);
        assertNotNull("leaf unreslovable-leafref is not present in root of module "+ module.getName(), condLeaf);
        assertTrue(condLeaf instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        provider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void provideTypeForLeafrefWithNullLeafrefTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        provider.provideTypeForLeafref(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void provideTypeForLeafrefWithNullLeafrefTypePathStatementTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final LeafrefTypeWithNullXpath leafrePath = new LeafrefTypeWithNullXpath();
        provider.provideTypeForLeafref(leafrePath, this.schemaNode);
    }

    @Test(expected = IllegalStateException.class)
    public void provideTypeForLeafrefWithNullParentModuleTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "bar",
            "leafref-value");
        final TypeDefinition<?> leafType = leaf.getType();
        assertTrue(leafType instanceof LeafrefTypeDefinition);
        doReturn(null).when(this.schemaNode).getPath();
        provider.provideTypeForLeafref((LeafrefTypeDefinition) leafType, this.schemaNode);
    }

    @Test
    public void javaTypeForSchemaDefinitionIdentityrefExtTypeTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "crypto");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ParameterizedType);
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithSimpleTypesTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "use-of-unions", "simple-int-types-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("YangUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtComplexUnionWithInnerUnionTypesTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "use-of-unions", "complex-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("ComplexUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithInnerUnionAndSimpleTypeTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "use-of-unions", "complex-string-int-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("ComplexStringIntUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionTypesTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module testTypeProvider = resolveModule("test-type-provider");
        final TypeDefinition<?> unionTypeDef = resolveTypeDefinitionFromModule(testTypeProvider, "complex-union");

        assertNotNull(unionTypeDef);
        assertTrue(unionTypeDef.getBaseType() instanceof UnionTypeDefinition);
        GeneratedTOBuilder unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef("test.package.name",
            (UnionTypeDefinition)unionTypeDef.getBaseType(), "ComplexUnionType", unionTypeDef);

        assertNotNull(unionTypeBuilder);

        GeneratedTransferObject unionType = unionTypeBuilder.build();
        assertEquals("ComplexUnionType", unionType.getName());

        unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef("test.package.name",
            (UnionTypeDefinition)unionTypeDef.getBaseType(), "", unionTypeDef);

        assertNotNull(unionTypeBuilder);

        unionType = unionTypeBuilder.build();
        assertEquals("Union", unionType.getName());

        unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef("test.package.name",
            (UnionTypeDefinition)unionTypeDef.getBaseType(), null, unionTypeDef);

        assertNotNull(unionTypeBuilder);

        unionType = unionTypeBuilder.build();
        assertEquals("Union", unionType.getName());
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionAndSimpleTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module testTypeProvider = resolveModule("test-type-provider");
        final TypeDefinition<?> unionTypeDef = resolveTypeDefinitionFromModule(testTypeProvider,
            "complex-string-int-union");

        assertNotNull(unionTypeDef);
        assertTrue(unionTypeDef.getBaseType() instanceof UnionTypeDefinition);
        final GeneratedTOBuilder unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef(
            "test.package.name",(UnionTypeDefinition)unionTypeDef.getBaseType(), "ComplexStringIntUnionType",
            unionTypeDef);

        assertNotNull(unionTypeBuilder);

        final GeneratedTransferObject unionType = unionTypeBuilder.build();
        assertEquals("ComplexStringIntUnionType", unionType.getName());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(this.schemaContext);

        final Module baseYangTypes = resolveModule("base-yang-types");
        final Set<TypeDefinition<?>> typeDefs = baseYangTypes.getTypeDefinitions();

        Type yangBoolean = null;
        Type yangEmpty = null;
        Type yangEnumeration = null;
        Type yangInt8 = null;
        Type yangInt8Restricted = null;
        Type yangInt16 = null;
        Type yangInt32 = null;
        Type yangInt64 = null;
        Type yangString = null;
        Type yangDecimal = null;
        Type yangUint8 = null;
        Type yangUint16 = null;
        Type yangUint32 = null;
        Type yangUint64 = null;
        Type yangUnion = null;
        Type yangBinary = null;
        Type yangBits = null;
        Type yangInstanceIdentifier = null;

        for (final TypeDefinition<?> typedef : typeDefs) {
            final Type type = provider.generatedTypeForExtendedDefinitionType(typedef, typedef);
            if (type instanceof GeneratedTransferObject) {
                if (type.getName().equals("YangBoolean")) {
                    yangBoolean = type;
                } else if (type.getName().equals("YangEmpty")) {
                    yangEmpty = type;
                } else if (type.getName().equals("YangInt8")) {
                    yangInt8 = type;
                } else if (type.getName().equals("YangInt8Restricted")) {
                    yangInt8Restricted = type;
                } else if (type.getName().equals("YangInt16")) {
                    yangInt16 = type;
                } else if (type.getName().equals("YangInt32")) {
                    yangInt32 = type;
                } else if (type.getName().equals("YangInt64")) {
                    yangInt64 = type;
                } else if (type.getName().equals("YangString")) {
                    yangString = type;
                } else if (type.getName().equals("YangDecimal64")) {
                    yangDecimal = type;
                } else if (type.getName().equals("YangUint8")) {
                    yangUint8 = type;
                } else if (type.getName().equals("YangUint16")) {
                    yangUint16 = type;
                } else if (type.getName().equals("YangUint32")) {
                    yangUint32 = type;
                } else if (type.getName().equals("YangUint64")) {
                    yangUint64 = type;
                } else if (type.getName().equals("YangUnion")) {
                    yangUnion = type;
                } else if (type.getName().equals("YangBinary")) {
                    yangBinary = type;
                } else if (type.getName().equals("YangInstanceIdentifier")) {
                    yangInstanceIdentifier = type;
                } else if (type.getName().equals("YangBits")) {
                    yangBits = type;
                }
            } else if (type instanceof Enumeration) {
                if (type.getName().equals("YangEnumeration")) {
                    yangEnumeration = type;
                }
            }
        }

        assertNotNull(yangBoolean);
        assertNotNull(yangEmpty);
        assertNotNull(yangEnumeration);
        assertNotNull(yangInt8);
        assertNotNull(yangInt8Restricted);
        assertNotNull(yangInt16);
        assertNotNull(yangInt32);
        assertNotNull(yangInt64);
        assertNotNull(yangString);
        assertNotNull(yangDecimal);
        assertNotNull(yangUint8);
        assertNotNull(yangUint16);
        assertNotNull(yangUint32);
        assertNotNull(yangUint64);
        assertNotNull(yangUnion);
        assertNotNull(yangBinary);
        assertNotNull(yangBits);
        assertNotNull(yangInstanceIdentifier);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedTypeForExtendedDefinitionTypeWithTypedefNullTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        provider.generatedTypeForExtendedDefinitionType(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedTypeForExtendedDefinitionTypeWithTypedefQNameNullTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final TestIntegerTypeDefinition testInt = new TestIntegerTypeDefinition();
        provider.generatedTypeForExtendedDefinitionType(testInt, testInt);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithInnerExtendedTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module baseYangTypes = resolveModule("test-type-provider");
        final TypeDefinition<?> extYangInt8Typedef = resolveTypeDefinitionFromModule(baseYangTypes,
            "extended-yang-int8");
        assertNotNull(extYangInt8Typedef);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(extYangInt8Typedef, extYangInt8Typedef);
        assertNotNull(extType);
        assertTrue(extType instanceof GeneratedTransferObject);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithLeafrefBaseTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module baseYangTypes = resolveModule("test-type-provider");
        final TypeDefinition<?> barItemLeafrefId = resolveTypeDefinitionFromModule(baseYangTypes, "bar-item-leafref-id");
        assertNotNull(barItemLeafrefId);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(barItemLeafrefId, barItemLeafrefId);
        assertEquals(null, extType);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithIdentityrefBaseTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final Module baseYangTypes = resolveModule("test-type-provider");

        final TypeDefinition<?> aesIdentityrefType = resolveTypeDefinitionFromModule(baseYangTypes, "aes-identityref-type");

        assertNotNull(aesIdentityrefType);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(aesIdentityrefType, aesIdentityrefType);
        assertEquals(null, extType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void provideGeneratedTOBuilderForBitsTypeDefinitionWithNullTypedefTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        provider.provideGeneratedTOBuilderForBitsTypeDefinition("", null, "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void provideGeneratedTOBuilderForBitsTypeDefinitionWithBasePackageNullTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();
        provider.provideGeneratedTOBuilderForBitsTypeDefinition(null, leafType, "", "");
    }

    @Test
    public void provideGeneratedTOBuilderForBitsTypeDefinitionWithNonBitsTypedefTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();
        final Type type = provider.provideGeneratedTOBuilderForBitsTypeDefinition("", leafType, "", "");

        assertEquals(null, type);
    }

    @Test
    public void getConstructorPropertyNameTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();

        final String ctorPropertyName = provider.getConstructorPropertyName(leafType);
        assertEquals("value", ctorPropertyName);

        final String emptyStringName = provider.getConstructorPropertyName(leaf);
        assertTrue(emptyStringName.isEmpty());
    }

    @Test
    public void getParamNameFromTypeTest() {
        final TypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();

        final String paramName = provider.getParamNameFromType(leafType);
        assertEquals("yangInt8", paramName);
    }

    @Test
    public void addUnitsToGenTOTest() {
        final GeneratedTOBuilder builder = new CodegenGeneratedTOBuilder("test.package", "TestBuilder");

        CodegenTypeProvider.addUnitsToGenTO(builder, null);
        GeneratedTransferObject genTO = builder.build();
        assertTrue(genTO.getConstantDefinitions().isEmpty());

        CodegenTypeProvider.addUnitsToGenTO(builder, "");
        genTO = builder.build();
        assertTrue(genTO.getConstantDefinitions().isEmpty());

        CodegenTypeProvider.addUnitsToGenTO(builder, "125");
        genTO = builder.build();
        assertTrue(!genTO.getConstantDefinitions().isEmpty());
        assertEquals(1, genTO.getConstantDefinitions().size());
        assertEquals("_UNITS", genTO.getConstantDefinitions().get(0).getName());
        assertEquals(genTO.getConstantDefinitions().get(0).getValue(), "\"125\"");
    }

    @Test(expected = NullPointerException.class)
    public void getTypeDefaultConstructionLeafTypeNullTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final TestLeafSchemaNode leafSchemaNode = new TestLeafSchemaNode();
        provider.getTypeDefaultConstruction(leafSchemaNode, null);
    }

    @Test(expected = NullPointerException.class)
    public void getTypeDefaultConstructionDefaultValueNullTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafForGetDefaultConstructionTestCase("yang-boolean");
        provider.getTypeDefaultConstruction(leaf, null);
    }

    private LeafSchemaNode provideLeafForGetDefaultConstructionTestCase(final String leafName) {
        return provideLeafNodeFromTopLevelContainer(this.testTypeProviderModule, "construction-type-test",
            leafName);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getTypeDefaultConstructionDefaultValueForInstanceIdentifierTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafForGetDefaultConstructionTestCase("foo-container-id");
        provider.getTypeDefaultConstruction(leaf, "NAN");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getTypeDefaultConstructionDefaultValueForIdentityrefTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);
        final LeafSchemaNode leaf = provideLeafForGetDefaultConstructionTestCase("aes-identityref-type");
        provider.getTypeDefaultConstruction(leaf, "NAN");
    }

    @Test
    public void getTypeDefaultConstructionDefaultValueTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(this.schemaContext);

        LeafSchemaNode leaf = provideLeafForGetDefaultConstructionTestCase("yang-boolean");
        String result = provider.getTypeDefaultConstruction(leaf, "true");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangBoolean(java.lang.Boolean.TRUE)",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-empty");
        result = provider.getTypeDefaultConstruction(leaf, "true");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangEmpty(java.lang.Boolean.TRUE)",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-enumeration");
        result = provider.getTypeDefaultConstruction(leaf, "a");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangEnumeration.A",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("direct-use-of-enum");
        result = provider.getTypeDefaultConstruction(leaf, "y");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.construction.type.test.DirectUseOfEnum.Y",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-int8");
        result = provider.getTypeDefaultConstruction(leaf, "17");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt8(java.lang.Byte.valueOf(\"17\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-int8-restricted");
        result = provider.getTypeDefaultConstruction(leaf, "99");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt8Restricted(java.lang.Byte.valueOf(\"99\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-int16");
        result = provider.getTypeDefaultConstruction(leaf, "1024");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt16(java.lang.Short.valueOf(\"1024\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-int32");
        result = provider.getTypeDefaultConstruction(leaf, "1048576");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt32(java.lang.Integer.valueOf(\"1048576\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-int64");
        result = provider.getTypeDefaultConstruction(leaf, "1099511627776");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt64(java.lang.Long.valueOf(\"1099511627776\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-string");
        result = provider.getTypeDefaultConstruction(leaf, "TEST");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangString(\"TEST\")",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-decimal64");
        result = provider.getTypeDefaultConstruction(leaf, "1274.25");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangDecimal64(new java.math.BigDecimal(\"1274.25\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-uint8");
        result = provider.getTypeDefaultConstruction(leaf, "128");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangUint8(java.lang.Short.valueOf(\"128\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-uint16");
        result = provider.getTypeDefaultConstruction(leaf, "1048576");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangUint16(java.lang.Integer.valueOf(\"1048576\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-uint32");
        result = provider.getTypeDefaultConstruction(leaf, "1099511627776");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangUint32(java.lang.Long.valueOf(\"1099511627776\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-uint64");
        result = provider.getTypeDefaultConstruction(leaf, "1208925819614629174706176");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangUint64(new java.math.BigInteger(\"1208925819614629174706176\"))",
            result);

        //FIXME: Is this correct scenario and correct result?
        leaf = provideLeafForGetDefaultConstructionTestCase("complex-union");
        result = provider.getTypeDefaultConstruction(leaf, "75");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.ComplexUnion(\"null\".toCharArray())",
            result);

        //FIXME: Is this correct scenario and correct result?
        leaf = provideLeafForGetDefaultConstructionTestCase("complex-string-int-union");
        result = provider.getTypeDefaultConstruction(leaf, "TEST_UNION_STRING_DEFAULT");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.ComplexStringIntUnion(\"null\".toCharArray())",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("simple-int-types-union");
        result = provider.getTypeDefaultConstruction(leaf, "2048");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangUnion(\"null\".toCharArray())",
            result);


        leaf = provideLeafForGetDefaultConstructionTestCase("direct-union-leaf");
        result = provider.getTypeDefaultConstruction(leaf);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.DirectUnionLeaf(\"128\".toCharArray())",
            result);

        final Module module = resolveModule("test-type-provider");
        final QName leafUnionNode = QName.create(module.getQNameModule(), "root-union-leaf");
        DataSchemaNode rootNode = module.getDataChildByName(leafUnionNode);
        assertNotNull("leaf root-union-leaf is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof LeafSchemaNode);
        leaf = (LeafSchemaNode) rootNode;
        result = provider.getTypeDefaultConstruction(leaf);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.TestTypeProviderData.RootUnionLeaf(\"256\".toCharArray())",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-binary");
        result = provider.getTypeDefaultConstruction(leaf, "0xffffff");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangBinary(new byte[] {-45, 23, -33, 125, -9, -33})",
            result);

        final QName leafBitsNode = QName.create(module.getQNameModule(), "root-bits-leaf");
        rootNode = module.getDataChildByName(leafBitsNode);
        assertNotNull("leaf bits-leaf is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof LeafSchemaNode);
        leaf = (LeafSchemaNode) rootNode;
        result = provider.getTypeDefaultConstruction(leaf);
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.TestTypeProviderData.RootBitsLeaf(false, true, false)",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("yang-bits");
        result = provider.getTypeDefaultConstruction(leaf, "10-Mb-only");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangBits(true, false, false)",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("bar-id");
        result = provider.getTypeDefaultConstruction(leaf, "128");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt16(java.lang.Short.valueOf(\"128\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("foo-leafref-value");
        result = provider.getTypeDefaultConstruction(leaf, "32");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914.YangInt8(java.lang.Byte.valueOf(\"32\"))",
            result);

        leaf = provideLeafForGetDefaultConstructionTestCase("foo-cond-bar-item");
        result = provider.getTypeDefaultConstruction(leaf, "10");
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals("new java.lang.Object()",
            result);
    }
}
