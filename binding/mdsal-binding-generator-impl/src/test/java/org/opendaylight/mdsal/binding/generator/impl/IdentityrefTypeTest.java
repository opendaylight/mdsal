/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class IdentityrefTypeTest {

    private static List<File> testModels = null;

    @Before
    public void loadTestResources() throws URISyntaxException {
        URI folderPath = IdentityrefTypeTest.class.getResource("/identityref.yang").toURI();
        File folderFile = new File(folderPath);
        testModels = new ArrayList<>();

        if (folderFile.isFile()) {
            testModels.add(folderFile);
        } else {
            for (File file : folderFile.listFiles()) {
                if (file.isFile()) {
                    testModels.add(file);
                }
            }
        }
    }

    /**
     * Test mainly for the method TypeProviderImpl#provideTypeForIdentityref(IdentityrefTypeDefinition)
     * provideTypeForIdentityref}.
     */
    @Test
    public void testIdentityrefYangBuiltInType() {
        final SchemaContext context = YangParserTestUtils.parseYangFiles(testModels);

        assertNotNull(context);
        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        GeneratedType moduleGenType = null;
        for (Type type : genTypes) {
            if (type.getName().equals("ModuleIdentityrefData")) {
                if (type instanceof GeneratedType) {
                    moduleGenType = (GeneratedType) type;
                }
            }
        }

        assertNotNull("Generated type for whole module wasn't found", moduleGenType);

        String typeName = null;
        String actualTypeName = "";
        int numOfActualTypes = 0;
        List<MethodSignature> methodSignatures = moduleGenType.getMethodDefinitions();
        for (MethodSignature methodSignature : methodSignatures) {
            if (methodSignature.getName().equals("getLf")) {
                Type returnType = methodSignature.getReturnType();
                if (returnType instanceof ParameterizedType) {
                    typeName = returnType.getName();
                    Type[] actualTypes = ((ParameterizedType) returnType).getActualTypeArguments();
                    numOfActualTypes = actualTypes.length;
                    actualTypeName = actualTypes[0].getName();
                }
            }
        }
        assertNotNull("The method 'getLf' was not found", typeName);
        assertEquals("Return type has incorrect name", "Class", typeName);
        assertEquals("Incorrect number of type parameters", 1, numOfActualTypes);
        assertEquals("Return type has incorrect actual parameter", "SomeIdentity", actualTypeName);
    }
}
