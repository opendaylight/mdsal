/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding2.generator.util.generated.type.builder.EnumerationBuilderImpl;
import org.opendaylight.mdsal.binding2.model.api.Enumeration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;

public class EnumGeneratorTest {
    private final Status status = Status.CURRENT;
    private final QName qName = QName.create("TestQName", "1-1-2016", "TestLocalQName");
    private final String description = "Test description of Enum";
    private final String packagename = "org.opendaylight.test";
    private final String name = "TestName";
    private final String moduleName = "TestModuleName";
    private final String reference = "TestRef";
    private final String valueName1 = "TestValue1";
    private final String valueName2 = "TestValue2";
    private final String valueDescription = "Value used for test";
    private final int value1 = 1;
    private final int value2 = 2;
    private Enumeration enumeration;
    private EnumerationBuilderImpl enumerationBuilder;

    @Before
    public void setup() {
        enumerationBuilder = new EnumerationBuilderImpl(packagename, name);
        enumerationBuilder.setDescription(description);
        enumerationBuilder.setModuleName(moduleName);
        enumerationBuilder.setReference(reference);
        enumerationBuilder.setSchemaPath(Collections.singletonList(qName));
        enumerationBuilder.addValue(valueName1, value1, valueDescription, reference, status);
        enumerationBuilder.addValue(valueName2, value2, valueDescription, reference, status);
        enumerationBuilder.addAnnotation(packagename, "TestAnnotation");
        enumeration = enumerationBuilder.toInstance(enumerationBuilder);
    }

    @Test
    public void enumGeneratorTest() {
        //        TO DO implement tests
        String enumGenerator = new EnumGenerator().generate(enumeration);
        System.out.println(enumGenerator);
    }
}