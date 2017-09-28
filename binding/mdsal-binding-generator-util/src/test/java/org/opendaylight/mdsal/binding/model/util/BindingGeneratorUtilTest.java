/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.util.type.StringTypeBuilder;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorUtilTest {
    private static final SchemaPath ROOT_PATH = SchemaPath.create(true, QName.create("/root"));

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static List<File> loadTestResources(final String testFile) {
        final List<File> testModels = new ArrayList<>();
        File listModelFile;
        try {
            listModelFile = new File(BindingGeneratorUtilTest.class.getResource(testFile).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to load sources from " + testFile);
        }
        testModels.add(listModelFile);
        return testModels;
    }

    /**
     * Tests methods:
     * &lt;ul&gt;
     * &lt;li&gt;moduleNamespaceToPackageName&lt;/li&gt; - with revision
     * &lt;li&gt;packageNameForGeneratedType&lt;/li&gt;
     * &lt;ul&gt;
     * &lt;li&gt;validateJavaPackage&lt;/li&gt;
     * &lt;/ul&gt;
     * &lt;li&gt;packageNameForTypeDefinition&lt;/li&gt; &lt;li&gt;moduleNamespaceToPackageName&lt;/li&gt;
     * - without revision &lt;/ul&gt;
     */
    @Test
    public void testBindingGeneratorUtilMethods() throws Exception {
        List<File> testModels = loadTestResources("/module.yang");

        final Set<Module> modules = YangParserTestUtils.parseYangSources(testModels).getModules();
        String packageName = "";
        Module module = null;
        for (Module m : modules) {
            module = m;
            break;
        }
        assertNotNull("Module can't be null", module);

        // test of the method moduleNamespaceToPackageName()
        packageName = BindingGeneratorUtil.moduleNamespaceToPackageName(module);
        assertEquals("Generated package name is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910", packageName);

        // test of the method packageNameForGeneratedType()
        DataNodeIterator it = new DataNodeIterator(module);
        List<ContainerSchemaNode> schemaContainers = it.allContainers();
        String subPackageNameForDataNode = "";
        for (ContainerSchemaNode containerSchemaNode : schemaContainers) {
            if (containerSchemaNode.getQName().getLocalName().equals("cont-inner")) {
                subPackageNameForDataNode = BindingGeneratorUtil.packageNameForGeneratedType(packageName,
                        containerSchemaNode.getPath());
                break;
            }
        }
        assertEquals("The name of the subpackage is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910.cont.outter",
                subPackageNameForDataNode);

        // test of the method packageNameForTypeDefinition
        Set<TypeDefinition<?>> typeDefinitions = module.getTypeDefinitions();
        String subPackageNameForTypeDefinition = "";
        TypeDefinition<?> firstTypeDef = null;

        for (TypeDefinition<?> tpDef : typeDefinitions) {
            if (tpDef.getQName().getLocalName().equals("tpdf")) {
                subPackageNameForTypeDefinition = BindingGeneratorUtil.packageNameForTypeDefinition(packageName, tpDef);
                firstTypeDef = tpDef;
                break;
            }
        }
        assertEquals("The name of the subpackage is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910",
                subPackageNameForTypeDefinition);

        // test method getRestrictions
        Restrictions restriction = BindingGeneratorUtil.getRestrictions(firstTypeDef);
        assertNotNull(restriction);

        // test method computeDefaultSUID
        GeneratedTypeBuilder genTypeBuilder = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        genTypeBuilder.addMethod("testMethod");
        genTypeBuilder.addAnnotation("org.opendaylight.yangtools.test.annotation", "AnnotationTest");
        genTypeBuilder.addEnclosingTransferObject("testObject");
        genTypeBuilder.addProperty("newProp");
        GeneratedTypeBuilder genType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "Type2");
        genTypeBuilder.addImplementsType(genType);
        long computedSUID = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder);

        GeneratedTypeBuilder genTypeBuilder2 = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test2", "TestType2");
        long computedSUID2 = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder2);
        assertNotEquals(computedSUID, computedSUID2);

        // test of exception part of the method moduleNamespaceToPackageName()
        Module moduleWithoutRevision = mock(Module.class);
        doReturn(null).when(moduleWithoutRevision).getQNameModule();
        try {
            BindingGeneratorUtil.moduleNamespaceToPackageName(moduleWithoutRevision);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#packageNameForTypeDefinition(String, TypeDefinition)
     * packageNameForTypeDefinition(String, TypeDefinition)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    @Deprecated
    public void testPackageNameForTypeDefinitionNullBasePackageName() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Base Package Name cannot be NULL!");
        BindingGeneratorUtil.packageNameForTypeDefinition(null, null);
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#packageNameForTypeDefinition(String, TypeDefinition)
     * packageNameForTypeDefinition(String, TypeDefinition)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    @Deprecated
    public void testPackageNameForTypeDefinitionNullTypeDefinition() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Type Definition reference cannot be NULL!");
        BindingGeneratorUtil.packageNameForTypeDefinition("test.package", null);
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#packageNameForGeneratedType(String, SchemaPath)
     * packageNameForGeneratedType(String, SchemaPath)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    public void testPackageNameForGeneratedTypeNullBasePackageName() {
        expectedEx.expect(NullPointerException.class);
        BindingGeneratorUtil.packageNameForGeneratedType(null, null);
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#packageNameForGeneratedType(String, SchemaPath)
     * packageNameForGeneratedType(String, SchemaPath)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    public void testPackageNameForGeneratedTypeNullSchemaPath() {
        expectedEx.expect(NullPointerException.class);
        BindingGeneratorUtil.packageNameForGeneratedType("test.package", null);
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#parseToClassName(String)
     * parseToClassName(String)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    public void testParseToClassNameNullValue() {
        String className = BindingGeneratorUtil.parseToClassName("test-class-name");
        assertEquals("TestClassName", className);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name can not be null");
        className = BindingGeneratorUtil.parseToClassName(null);
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#parseToClassName(String)
     * parseToClassName(String)}&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Test
    public void testParseToClassNameEmptyValue() {
        String className = BindingGeneratorUtil.parseToClassName("test-class-name");
        assertEquals("TestClassName", className);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name can not be empty");
        className = BindingGeneratorUtil.parseToClassName("");
    }

    /**
     * Test for the method
     * &lt;ul&gt;
     * &lt;li&gt;{@link BindingGeneratorUtil#resolveJavaReservedWordEquivalency(String)
     * resolveJavaReservedWordEquivalency(String)}&lt;/li&gt;
     * &lt;ul&gt;
     */
    @Test
    public void testValidateParameterName() {
        assertNull("Return value is incorrect.", BindingGeneratorUtil.resolveJavaReservedWordEquivalency(null));
        assertEquals("Return value is incorrect.", "whatever",
                BindingGeneratorUtil.resolveJavaReservedWordEquivalency("whatever"));
        assertEquals("Return value is incorrect.", "_case",
                BindingGeneratorUtil.resolveJavaReservedWordEquivalency("case"));
    }

    /**
     * Tests the methods:
     * &lt;ul&gt;
     * &lt;li&gt;parseToClassName&lt;/li&gt;
     * &lt;ul&gt;
     * &lt;li&gt;parseToCamelCase&lt;/li&gt;
     * &lt;ul&gt;
     * &lt;li&gt;replaceWithCamelCase&lt;/li&gt;
     * &lt;/ul&gt;
     * &lt;/ul&gt; &lt;li&gt;parseToValidParamName&lt;/li&gt;
     * &lt;ul&gt;
     * &lt;li&gt;parseToCamelCase&lt;/li&gt;
     * &lt;ul&gt;
     * &lt;li&gt;replaceWithCamelCase&lt;/li&gt;
     * &lt;/ul&gt;
     * &lt;/ul&gt;
     * &lt;ul&gt;
     */
    @Test
    public void testParsingMethods() {
        // parseToClassName method testing
        assertEquals("Class name has incorrect format", "SomeTestingClassName",
                BindingMapping.getClassName("  some-testing_class name   "));
        assertEquals("Class name has incorrect format", "_0SomeTestingClassName",
                BindingMapping.getClassName("  0 some-testing_class name   "));

        // parseToValidParamName
        assertEquals("Parameter name has incorrect format", "someTestingParameterName",
                BindingGeneratorUtil.parseToValidParamName("  some-testing_parameter   name   "));
        assertEquals("Parameter name has incorrect format", "_0someTestingParameterName",
                BindingGeneratorUtil.parseToValidParamName("  0some-testing_parameter   name   "));
    }

    @Test
    public void computeDefaultSUIDTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        MethodSignatureBuilder method = generatedTypeBuilder.addMethod("myMethodName");
        method.setAccessModifier(AccessModifier.PUBLIC);
        generatedTypeBuilder.addProperty("myProperty");
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));

        assertEquals(6788238694991761868L, BindingGeneratorUtil.computeDefaultSUID(generatedTypeBuilder));

    }

    @Test
    public void getRestrictionsTest() {
        final Optional<String> absent = Optional.absent();
        final StringTypeBuilder builder =
                RestrictedTypes.newStringBuilder(BaseTypes.stringType(), ROOT_PATH);

        builder.addPatternConstraint(BaseConstraints.newPatternConstraint(".*", absent, absent));
        builder.setLengthAlternatives(ImmutableList.of(
            BaseConstraints.newLengthConstraint(1, 2, absent, absent)));

        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(builder.build());

        assertNotNull(restrictions);

        assertEquals(1, restrictions.getLengthConstraints().size());
        assertEquals(0, restrictions.getRangeConstraints().size());
        assertEquals(1, restrictions.getPatternConstraints().size());

        assertFalse(restrictions.isEmpty());
        assertTrue(restrictions.getLengthConstraints().contains(
                BaseConstraints.newLengthConstraint(1, 2, absent, absent)));
        assertTrue(restrictions.getPatternConstraints().contains(
                BaseConstraints.newPatternConstraint(".*", absent, absent)));
    }

    @Test
    public void getEmptyRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.stringType(), ROOT_PATH).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
    }

    @Test
    public void getDefaultIntegerRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.int16Type(), ROOT_PATH).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((IntegerTypeDefinition) type.getBaseType()).getRangeConstraints(),
                restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void getDefaultUnsignedIntegerRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.uint16Type(), ROOT_PATH).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((UnsignedIntegerTypeDefinition) type.getBaseType()).getRangeConstraints(),
                restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void getDefaultDecimalRestrictionsTest() {
        final DecimalTypeDefinition base = BaseTypes.decimalTypeBuilder(ROOT_PATH).setFractionDigits(10).build();
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(base, ROOT_PATH).build();

        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(base.getRangeConstraints(), restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }
}
