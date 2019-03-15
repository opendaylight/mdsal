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
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.model.util.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.util.type.StringTypeBuilder;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorUtilTest {
    private static final SchemaPath ROOT_PATH = SchemaPath.create(true, QName.create("test", "root"));

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    /*
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
    public void testBindingGeneratorUtilMethods() {
        final Set<Module> modules = YangParserTestUtils.parseYangResources(BindingGeneratorUtilTest.class,
            "/module.yang").getModules();
        String packageName = "";
        Module module = null;
        for (Module m : modules) {
            module = m;
            break;
        }
        assertNotNull("Module can't be null", module);

        // test of the method moduleNamespaceToPackageName()
        packageName = BindingMapping.getRootPackageName(module.getQNameModule());
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

        // test method computeDefaultSUID
        GeneratedTypeBuilder genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"));
        genTypeBuilder.addMethod("testMethod");
        genTypeBuilder.addAnnotation("org.opendaylight.yangtools.test.annotation", "AnnotationTest");
        genTypeBuilder.addEnclosingTransferObject("testObject");
        genTypeBuilder.addProperty("newProp");
        GeneratedTypeBuilder genType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Type2"));
        genTypeBuilder.addImplementsType(genType);
        long computedSUID = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder);

        GeneratedTypeBuilder genTypeBuilder2 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test2", "TestType2"));
        long computedSUID2 = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder2);
        assertNotEquals(computedSUID, computedSUID2);
    }

    /*
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

    /*
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

    /*
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

    /*
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
        // getClassName method testing
        assertEquals("Class name has incorrect format", "SomeTestingClassName",
            BindingMapping.getClassName("  some-testing_class name   "));
        assertEquals("Class name has incorrect format", "_0SomeTestingClassName",
            BindingMapping.getClassName("  0 some-testing_class name   "));

        // getPropertyName
        assertEquals("Parameter name has incorrect format", "someTestingParameterName",
            BindingMapping.getPropertyName("  some-testing_parameter   name   "));
        assertEquals("Parameter name has incorrect format", "_0someTestingParameterName",
            BindingMapping.getPropertyName("  0some-testing_parameter   name   "));
    }

    @Test
    public void computeDefaultSUIDTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        MethodSignatureBuilder method = generatedTypeBuilder.addMethod("myMethodName");
        method.setAccessModifier(AccessModifier.PUBLIC);
        generatedTypeBuilder.addProperty("myProperty");
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));

        assertEquals(6788238694991761868L, BindingGeneratorUtil.computeDefaultSUID(generatedTypeBuilder));

    }

    @Test
    public void getRestrictionsTest() throws InvalidLengthConstraintException {
        final Optional<String> absent = Optional.empty();
        final StringTypeBuilder builder =
                RestrictedTypes.newStringBuilder(BaseTypes.stringType(), ROOT_PATH);

        builder.addPatternConstraint(BaseConstraints.newPatternConstraint(".*", absent, absent));
        builder.setLengthConstraint(mock(ConstraintMetaDefinition.class), ImmutableList.of(ValueRange.of(1, 2)));

        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(builder.build());

        assertNotNull(restrictions);
        assertEquals(ImmutableSet.of(Range.closed(1, 2)),
            restrictions.getLengthConstraint().get().getAllowedRanges().asRanges());
        assertFalse(restrictions.getRangeConstraint().isPresent());
        assertEquals(1, restrictions.getPatternConstraints().size());

        assertFalse(restrictions.isEmpty());
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
        assertEquals(((Int16TypeDefinition) type.getBaseType()).getRangeConstraint(),
                restrictions.getRangeConstraint());
        assertFalse(restrictions.getLengthConstraint().isPresent());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void getDefaultUnsignedIntegerRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.uint16Type(), ROOT_PATH).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((Uint16TypeDefinition) type.getBaseType()).getRangeConstraint(),
                restrictions.getRangeConstraint());
        assertFalse(restrictions.getLengthConstraint().isPresent());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void getDefaultDecimalRestrictionsTest() {
        final DecimalTypeDefinition base = BaseTypes.decimalTypeBuilder(ROOT_PATH).setFractionDigits(10).build();
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(base, ROOT_PATH).build();

        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(base.getRangeConstraint(), restrictions.getRangeConstraint());
        assertFalse(restrictions.getLengthConstraint().isPresent());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void unicodeCharReplaceTest() {
        String inputString = "abcu\\uuuuu\\uuua\\u\\\\uabc\\\\uuuu\\\\\\uuuu\\\\\\\\uuuu///uu/u/u/u/u/u/u";

        assertEquals("abcu\\\\uuuuu\\\\uuua\\\\u\\\\uabc\\\\uuuu\\\\uuuu\\\\uuuu///uu/u/u/u/u/u/u",
            BindingGeneratorUtil.replaceAllIllegalChars(inputString));
    }
}
