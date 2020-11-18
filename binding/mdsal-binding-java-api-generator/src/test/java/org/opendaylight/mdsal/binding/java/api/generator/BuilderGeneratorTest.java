/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BuilderGeneratorTest {
    private static final String LS = System.lineSeparator();
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    public void basicTest() {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateHashcodeWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#hashCode()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing" + LS
                + " * results across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate hashCode() result." + LS
                + " * @return Hash code value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static int bindingHashCode(final test.@NonNull test obj) {" + LS
                + "    final int prime = 31;" + LS
                + "    int result = 1;" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest());" + LS
                + "    return result;" + LS
                + "}" + LS, genHashCode(genType).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutAnyPropertyTest() throws Exception {
        assertEquals("", genHashCode(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#hashCode()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing" + LS
                + " * results across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate hashCode() result." + LS
                + " * @return Hash code value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static int bindingHashCode(final test.@NonNull test obj) {" + LS
                + "    final int prime = 31;" + LS
                + "    int result = 1;" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest1());" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest2());" + LS
                + "    return result;" + LS
                + "}" + LS, genHashCode(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#hashCode()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing" + LS
                + " * results across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate hashCode() result." + LS
                + " * @return Hash code value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static int bindingHashCode(final test.@NonNull test obj) {" + LS
                + "    final int prime = 31;" + LS
                + "    int result = 1;" + LS
                + "    result = prime * result + obj.augmentations().hashCode();" + LS
                + "    return result;" + LS
                + "}" + LS, genHashCode(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithPropertyWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#hashCode()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing" + LS
                + " * results across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate hashCode() result." + LS
                + " * @return Hash code value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static int bindingHashCode(final test.@NonNull test obj) {" + LS
                + "    final int prime = 31;" + LS
                + "    int result = 1;" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest());" + LS
                + "    result = prime * result + obj.augmentations().hashCode();" + LS
                + "    return result;" + LS
                + "}" + LS, genHashCode(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#hashCode()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing" + LS
                + " * results across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate hashCode() result." + LS
                + " * @return Hash code value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static int bindingHashCode(final test.@NonNull test obj) {" + LS
                + "    final int prime = 31;" + LS
                + "    int result = 1;" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest1());" + LS
                + "    result = prime * result + Objects.hashCode(obj.getTest2());" + LS
                + "    result = prime * result + obj.augmentations().hashCode();" + LS
                + "    return result;" + LS
                + "}" + LS, genHashCode(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    CodeHelpers.appendValue(helper, \"test\", obj.gettest());" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    CodeHelpers.appendValue(helper, \"test1\", obj.gettest1());" + LS
                + "    CodeHelpers.appendValue(helper, \"test2\", obj.gettest2());" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    CodeHelpers.appendValue(helper, \"test\", obj.gettest());" + LS
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("/**" + LS
                + " * Default implementation of {@link Object#toString()} contract for this interface." + LS
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + LS
                + " * representations across all implementations." + LS
                + " *" + LS
                + " * @param obj Object for which to generate toString() result." + LS
                + " * @return {@link String} value of data modeled by this interface." + LS
                + " * @throws NullPointerException if {@code obj} is null" + LS
                + " */" + LS
                + "static String bindingToString(final test.@NonNull test obj) {" + LS
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");" + LS
                + "    CodeHelpers.appendValue(helper, \"test1\", obj.gettest1());" + LS
                + "    CodeHelpers.appendValue(helper, \"test2\", obj.gettest2());" + LS
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());" + LS
                + "    return helper.toString();" + LS
                + "}" + LS, genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToEqualsComparingOrderTest() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource(
                "/test-types.yang");
        final List<Type> types = new DefaultBindingGenerator().generateTypes(context);
        final BuilderTemplate bt = BuilderGenerator.templateForType((GeneratedType) types.get(19));

        final List<String> sortedProperties = bt.properties.stream()
                .sorted(ByTypeMemberComparator.getInstance())
                .map(BuilderGeneratedProperty::getName)
                .collect(Collectors.toList());

        assertEquals(List.of(
                // numeric types (boolean, byte, short, int, long, biginteger, bigdecimal), identityrefs, empty
                "id16", "id16Def", "id32", "id32Def", "id64", "id64Def", "id8", "id8Def", "idBoolean", "idBooleanDef",
                "idDecimal64", "idDecimal64Def","idEmpty", "idEmptyDef", "idIdentityref", "idIdentityrefDef",
                "idLeafref", "idLeafrefDef", "idU16", "idU16Def", "idU32", "idU32Def", "idU64", "idU64Def", "idU8",
                "idU8Def",
                // string, binary, bits
                "idBinary", "idBinaryDef", "idBits", "idBitsDef", "idGroupLeafString", "idLeafrefContainer1",
                "idLeafrefContainer1Def", "idString", "idStringDef",
                // instance identifier
                "idInstanceIdentifier", "idInstanceIdentifierDef",
                // other types
                "idContainer1", "idContainer2", "idEnumeration", "idEnumerationDef",
                "idGroupContainer", "idList", "idUnion", "idUnionDef"), sortedProperties);
    }

    private static GeneratedType mockAugment(final GeneratedType genType) {
        final List<Type> impls = new ArrayList<>();
        final Type impl = mock(Type.class);
        doReturn("org.opendaylight.yangtools.yang.binding.Augmentable").when(impl).getFullyQualifiedName();
        impls.add(impl);
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static GeneratedType mockGenTypeMoreMeth(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).getIdentifier();
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();

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

    private static CharSequence genToString(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingToString();
    }

    private static CharSequence genHashCode(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingHashCode();
    }

    private static GeneratedType mockGenType(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).getIdentifier();
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        final MethodSignature methSign = mockMethSign(methodeName);
        listMethodSign.add(methSign);
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = mock(Type.class);
        doReturn(TYPE_NAME).when(methType).getIdentifier();
        doReturn(TEST).when(methType).getName();
        doReturn(methType).when(methSign).getReturnType();
        doReturn(ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }
}
