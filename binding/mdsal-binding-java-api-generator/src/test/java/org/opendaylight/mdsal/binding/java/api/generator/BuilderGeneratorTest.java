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
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    public void basicTest() {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test\", _test);\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n"
                + "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test\", _test);\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n"
                + "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToEqualsComparingOrderTest() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource(
                "/test-types.yang");
        final List<Type> types = new DefaultBindingGenerator().generateTypes(context);

        BuilderTemplate bt = BuilderGenerator.templateForType((GeneratedType) types.get(19));
        final BuilderGeneratedProperty[] properties = bt.properties.toArray(new BuilderGeneratedProperty[] {});
        // numeric types (boolean, byte, short, int, long, biginteger, bigdecimal), identityrefs
        assertEquals("id16", properties[0].getName());
        assertEquals("id16Def", properties[1].getName());
        assertEquals("id32", properties[2].getName());
        assertEquals("id32Def", properties[3].getName());
        assertEquals("id64", properties[4].getName());
        assertEquals("id64Def", properties[5].getName());
        assertEquals("id8", properties[6].getName());
        assertEquals("id8Def", properties[7].getName());
        assertEquals("idBoolean", properties[8].getName());
        assertEquals("idBooleanDef", properties[9].getName());
        assertEquals("idDecimal64", properties[10].getName());
        assertEquals("idDecimal64Def", properties[11].getName());
        assertEquals("idIdentityref", properties[12].getName());
        assertEquals("idIdentityrefDef", properties[13].getName());
        assertEquals("idLeafref", properties[14].getName());
        assertEquals("idLeafrefDef", properties[15].getName());
        assertEquals("idU16", properties[16].getName());
        assertEquals("idU16Def", properties[17].getName());
        assertEquals("idU32", properties[18].getName());
        assertEquals("idU32Def", properties[19].getName());
        assertEquals("idU64", properties[20].getName());
        assertEquals("idU64Def", properties[21].getName());
        assertEquals("idU8", properties[22].getName());
        assertEquals("idU8Def", properties[23].getName());
        // string, binary, bits
        assertEquals("idBinary", properties[24].getName());
        assertEquals("idBinaryDef", properties[25].getName());
        assertEquals("idBits", properties[26].getName());
        assertEquals("idBitsDef", properties[27].getName());
        assertEquals("idGroupLeafString", properties[28].getName());
        assertEquals("idLeafrefContainer1", properties[29].getName());
        assertEquals("idLeafrefContainer1Def", properties[30].getName());
        assertEquals("idString", properties[31].getName());
        assertEquals("idStringDef", properties[32].getName());
        // instance identifier
        assertEquals("idInstanceIdentifier", properties[33].getName());
        assertEquals("idInstanceIdentifierDef", properties[34].getName());
        // other types
        assertEquals("idContainer1", properties[35].getName());
        assertEquals("idContainer2", properties[36].getName());
        assertEquals("idEmpty", properties[37].getName());
        assertEquals("idEmptyDef", properties[38].getName());
        assertEquals("idEnumeration", properties[39].getName());
        assertEquals("idEnumerationDef", properties[40].getName());
        assertEquals("idGroupContainer", properties[41].getName());
        assertEquals("idList", properties[42].getName());
        assertEquals("idUnion", properties[43].getName());
        assertEquals("idUnionDef", properties[44].getName());
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
        final BuilderTemplate bt = BuilderGenerator.templateForType(genType);
        return bt.generateToString(bt.properties);
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
        doReturn(methType).when(methSign).getReturnType();
        doReturn(ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }
}
