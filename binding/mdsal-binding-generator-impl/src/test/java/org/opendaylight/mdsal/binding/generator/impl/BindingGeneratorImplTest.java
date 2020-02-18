/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorImplTest {

    @Test
    public void isisTotpologyStatementParserTest()  {
        SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/isis-topology/network-topology@2013-10-21.yang", "/isis-topology/isis-topology@2013-10-21.yang",
            "/isis-topology/l3-unicast-igp-topology@2013-10-21.yang");
        assertNotNull(context);

        List<Type> generateTypes = DefaultBindingGenerator.generateFor(context);
        assertFalse(generateTypes.isEmpty());
    }

    @Test
    public void choiceNodeGenerationTest() {
        SchemaContext context = YangParserTestUtils.parseYangResource("/binding-generator-impl-test/choice-test.yang");

        List<Type> generateTypes = DefaultBindingGenerator.generateFor(context);

        GeneratedType choiceTestData = null;
        GeneratedType myRootContainer = null;
        GeneratedType myList = null;
        GeneratedType myContainer = null;
        GeneratedType myList2 = null;
        GeneratedType myContainer2 = null;

        for (Type type : generateTypes) {
            switch (type.getName()) {
                case "ChoiceTestData":
                    choiceTestData = (GeneratedType) type;
                    break;
                case "Myrootcontainer":
                    myRootContainer = (GeneratedType) type;
                    break;
                case "Mylist":
                    myList = (GeneratedType) type;
                    break;
                case "Mylist2":
                    myList2 = (GeneratedType) type;
                    break;
                case "Mycontainer":
                    myContainer = (GeneratedType) type;
                    break;
                case "Mycontainer2":
                    myContainer2 = (GeneratedType) type;
                    break;
                default:
                    // ignore
            }
        }

        assertNotNull(choiceTestData);
        assertNotNull(myRootContainer);
        assertNotNull(myList);
        assertNotNull(myContainer);
        assertNotNull(myList2);
        assertNotNull(myContainer2);

        List<Type> implements1 = myContainer.getImplements();
        Type childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("ChoiceTestData"));

        implements1 = myList.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("ChoiceTestData"));

        implements1 = myContainer2.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("Myrootcontainer"));

        implements1 = myList2.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("Myrootcontainer"));

    }

    @Test
    public void notificationGenerationTest() {
        SchemaContext context = YangParserTestUtils.parseYangResource(
            "/binding-generator-impl-test/notification-test.yang");

        List<Type> generateTypes = DefaultBindingGenerator.generateFor(context);

        GeneratedType foo = null;
        for (Type type : generateTypes) {
            if (type.getName().equals("Foo")) {
                foo = (GeneratedType) type;
                break;
            }
        }

        Type childOf = null;
        Type dataObject = null;
        List<Type> impl = foo.getImplements();
        for (Type type : impl) {
            switch (type.getName()) {
                case "ChildOf":
                    childOf = type;
                    break;
                case "DataObject":
                    dataObject = type;
                    break;
                default:
                    // ignore
            }
        }

        assertNull(childOf);
        assertNotNull(dataObject);
    }

}
