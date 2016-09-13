/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding2.model.api.Type;

public class InterfaceGeneratorTest {
    private Type genType;

    @Before
    public void setup() {
        final GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("test.package.name", "testName");
        generatedTypeBuilder.setDescription("test description");
        genType = generatedTypeBuilder.toInstance();
    }

    @Test
    public void interfaceGeneratorTest() {
        String InterfaceGenerator = new InterfaceGenerator().generate(genType);
        System.out.println(InterfaceGenerator);
    }
}