/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public class BuilderGeneratorTest {

    private static final String PROPERTIES_FIELD_NAME = "properties";
    private final String firstPartOfGenToStringMethod_wihtoutWS =
            "@Overridepublicjava.lang.StringtoString(){java.lang.StringBuilderbuilder=newjava.lang.StringBuilder("
                    + "\"test[\");";
    private final String lastPartOfGenToStringMethode_wihtoutWS = "returnbuilder.append(']').toString();}";
    private final String argPartOfGenToStringMethode_withoutWS =
            "builder.append(\"augmentation=\");builder.append(augmentation.values());";
    private final String comaPartOfAppend = "builder.append(\",\");";
    private final String TEST = "test";

    @Test
    public void basicTest() throws Exception {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplate_generateToStringWithPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + this.TEST);
        final String generateToString = genToString_withoutWS(genType);
        System.out.println(generateToString);
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod_wihtoutWS
                + "if(_test!=null){builder.append(\"_test=\");builder.append(_test);}"
                + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    @Test
    public void builderTemplate_generateToStringWithoutAnyPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType(this.TEST);
        final String generateToString = genToString_withoutWS(genType).toString();
        assertTrue(generateToString
                .contains(this.firstPartOfGenToStringMethod_wihtoutWS + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    @Test
    public void builderTemplate_generateToStringWithMorePropertiesTest() throws Exception {
        final GeneratedType genType = mockGenType_moreMeth("get" + this.TEST);
        final String generateToString = genToString_withoutWS(genType).toString();
        System.out.println(generateToString);
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod_wihtoutWS
                + "if(_test1!=null){builder.append(\"_test1=\");builder.append(_test1);" + this.comaPartOfAppend + "}"
                + "if(_test2!=null){builder.append(\"_test2=\");builder.append(_test2);}"
                + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    @Test
    public void builderTemplate_generateToStringWithoutPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType(this.TEST);
        mockAugment(genType);
        final String generateToString = genToString_withoutWS(genType).toString();
        System.out.println(generateToString);
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod_wihtoutWS
                + this.argPartOfGenToStringMethode_withoutWS + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    @Test
    public void builderTemplate_generateToStringWithPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + this.TEST);
        mockAugment(genType);
        final String generateToString = genToString_withoutWS(genType).toString();
        System.out.println(generateToString);
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod_wihtoutWS
                + "if(_test!=null){builder.append(\"_test=\");builder.append(_test);}" + this.comaPartOfAppend
                + this.argPartOfGenToStringMethode_withoutWS + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    @Test
    public void builderTemplate_generateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType_moreMeth("get" + this.TEST);
        mockAugment(genType);
        final String generateToString = genToString_withoutWS(genType).toString();
        System.out.println(generateToString);
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod_wihtoutWS
                + "if(_test1!=null){builder.append(\"_test1=\");builder.append(_test1);" + this.comaPartOfAppend + "}"
                + "if(_test2!=null){builder.append(\"_test2=\");builder.append(_test2);}" + this.comaPartOfAppend
                + this.argPartOfGenToStringMethode_withoutWS + this.lastPartOfGenToStringMethode_wihtoutWS));
    }

    private void mockAugment(final GeneratedType genType) {
        final List<Type> impls = new ArrayList<>();
        final Type impl = mock(Type.class);
        doReturn("org.opendaylight.yangtools.yang.binding.Augmentable").when(impl).getFullyQualifiedName();
        impls.add(impl);
        doReturn(impls).when(genType).getImplements();
    }

    private GeneratedType mockGenType_moreMeth(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(this.TEST).when(genType).getName();
        doReturn(this.TEST).when(genType).getPackageName();

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

    private String genToString_withoutWS(final GeneratedType genType)
            throws NoSuchFieldException, IllegalAccessException {
        final BuilderTemplate bt = new BuilderTemplate(genType);
        final Field propertiesField = bt.getClass().getDeclaredField(PROPERTIES_FIELD_NAME);
        propertiesField.setAccessible(true);
        return bt.generateToString((Collection<GeneratedProperty>) propertiesField.get(bt)).toString()
                .replaceAll(" ", "").replaceAll("\n", "");
    }

    private GeneratedType mockGenType(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(this.TEST).when(genType).getName();
        doReturn(this.TEST).when(genType).getPackageName();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        final MethodSignature methSign = mockMethSign(methodeName);
        listMethodSign.add(methSign);
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = mock(Type.class);
        doReturn(this.TEST).when(methType).getName();
        doReturn(this.TEST).when(methType).getPackageName();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
