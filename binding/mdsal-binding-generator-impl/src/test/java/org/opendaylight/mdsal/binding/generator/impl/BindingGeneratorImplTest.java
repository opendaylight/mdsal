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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorImplTest {

    @Test
    public void isisTotpologyStatementParserTest() throws IOException,
            URISyntaxException, ReactorException {
        final InputStream topo = getClass().getResourceAsStream("/isis-topology/network-topology@2013-10-21.yang");
        final InputStream isis = getClass().getResourceAsStream("/isis-topology/isis-topology@2013-10-21.yang");
        final InputStream l3 = getClass().getResourceAsStream("/isis-topology/l3-unicast-igp-topology@2013-10-21.yang");

        SchemaContext context = YangParserTestUtils.parseYangStreams(ImmutableList.of(isis, l3, topo));
        assertNotNull(context);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

        assertFalse(generateTypes.isEmpty());
    }

    @Test
    public void choiceNodeGenerationTest() throws IOException,
            URISyntaxException, ReactorException {
        File resourceFile = new File(getClass().getResource(
                "/binding-generator-impl-test/choice-test.yang").toURI());

        SchemaContext context = YangParserTestUtils.parseYangSources(resourceFile);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

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
    public void notificationGenerationTest() throws IOException, URISyntaxException, ReactorException {
        File resourceFile = new File(getClass().getResource(
                "/binding-generator-impl-test/notification-test.yang").toURI());

        SchemaContext context = YangParserTestUtils.parseYangSources(resourceFile);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

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
            }
        }

        assertNull(childOf);
        assertNotNull(dataObject);
    }

}
