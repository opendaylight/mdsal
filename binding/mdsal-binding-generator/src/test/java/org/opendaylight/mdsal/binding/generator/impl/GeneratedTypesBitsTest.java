/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesBitsTest {
    @Test
    public void testGeneratedTypesBitsTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-bits-demo.yang"));
        assertTrue(genTypes != null);

        List<MethodSignature> methodSignaturesList = null;

        boolean leafParentFound = false;

        boolean byteTypeFound = false;
        int classPropertiesNumb = 0;
        int toStringPropertiesNum = 0;
        int equalPropertiesNum = 0;
        int hashPropertiesNum = 0;

        String nameReturnParamType = "";
        boolean getByteLeafMethodFound = false;
        boolean setByteLeafMethodFound = false;
        int setByteLeafMethodParamNum = 0;

        for (final GeneratedType type : genTypes) {
            if (type instanceof GeneratedTransferObject) {
                // searching for ByteType
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
                if (genTO.getName().equals("ByteType")) {
                    byteTypeFound = true;
                    List<GeneratedProperty> genProperties = genTO.getProperties();
                    classPropertiesNumb = genProperties.size();

                    genProperties = genTO.getToStringIdentifiers();
                    toStringPropertiesNum = genProperties.size();

                    genProperties = genTO.getEqualsIdentifiers();
                    equalPropertiesNum = genProperties.size();

                    genProperties = genTO.getHashCodeIdentifiers();
                    hashPropertiesNum = genProperties.size();

                }
            } else {
                // searching for interface LeafParameterContainer
                final GeneratedType genType = type;
                if (genType.getName().equals("LeafParentContainer")) {
                    leafParentFound = true;
                    // check of methods
                    methodSignaturesList = genType.getMethodDefinitions();
                    if (methodSignaturesList != null) {
                        // loop through all methods
                        for (MethodSignature methodSignature : methodSignaturesList) {
                            if (methodSignature.getName().equals("getByteLeaf")) {
                                getByteLeafMethodFound = true;

                                nameReturnParamType = methodSignature.getReturnType().getName();
                            } else if (methodSignature.getName().equals("setByteLeaf")) {
                                setByteLeafMethodFound = true;

                                List<Parameter> parameters = methodSignature.getParameters();
                                setByteLeafMethodParamNum = parameters.size();
                            }
                        }
                    }
                }
            }
        }

        assertTrue(byteTypeFound);

        assertEquals(1, classPropertiesNumb);

        assertEquals(1, toStringPropertiesNum);
        assertEquals(1, equalPropertiesNum);
        assertEquals(1, hashPropertiesNum);
        assertTrue(leafParentFound);

        assertNotNull(methodSignaturesList);

        assertTrue(getByteLeafMethodFound);
        assertEquals("ByteType", nameReturnParamType);

        assertFalse(setByteLeafMethodFound);
        assertEquals(0, setByteLeafMethodParamNum);
    }

    @Test
    public void testGeneratedTypesRestrictedBitsTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/simple-restricted-bits-demo.yang"));
        assertNotNull(genTypes);

        boolean myBitsFound = false;
        boolean myBitsRestrictedFound = false;
        Map<Uint32, String> validMyBits = ImmutableMap.of(Uint32.valueOf(0), "bitZero",
                Uint32.valueOf(1), "bitOne", Uint32.valueOf(2), "bitTwo");
        Map<Uint32, String> validMyBitsRestricted = ImmutableMap.of(Uint32.valueOf(0), "bitZero",
                Uint32.valueOf(1), "bitOne");

        for (final GeneratedType type : genTypes) {
            if (type instanceof final GeneratedTransferObject genTO) {
                if (genTO.getName().equals("MyBits")) {
                    myBitsFound = true;
                    assertEquals(1, genTO.getProperties().size());
                    assertEquals(validMyBits, genTO.getConstantDefinitions().get(0).getValue());
                    assertEquals(1, genTO.getToStringIdentifiers().size());
                    assertEquals(1, genTO.getEqualsIdentifiers().size());
                    assertEquals(1, genTO.getHashCodeIdentifiers().size());
                } else if (genTO.getName().equals("MyBitsRestricted")) {
                    myBitsRestrictedFound = true;
                    assertEquals("MyBits", genTO.getSuperType().getName());
                    assertEquals(validMyBitsRestricted, genTO.getConstantDefinitions().get(0).getValue());
                    assertEquals(0, genTO.getProperties().size());
                    assertEquals(0, genTO.getToStringIdentifiers().size());
                    assertEquals(0, genTO.getEqualsIdentifiers().size());
                    assertEquals(0, genTO.getHashCodeIdentifiers().size());
                }
            }
        }
        assertTrue(myBitsFound);
        assertTrue(myBitsRestrictedFound);
    }

    @Test
    public void testBitsTypeObjectStorage() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/test-bits-type-object-storage.yang"));
        assertNotNull(genTypes);

        boolean bitsIntFound = false;
        boolean bitsLongFound = false;
        boolean bitsIntArrayFound = false;

        for (final GeneratedType type : genTypes) {
            if (type instanceof final GeneratedTransferObject genTO) {
                assertTrue(genTO.getImplements().stream().anyMatch(impl -> impl.getName().equals("BitsTypeObject")));
                if (genTO.getName().equals("BitsInt")) {
                    bitsIntFound = true;
                    assertEquals(1, genTO.getProperties().size());
                    assertEquals(Types.primitiveIntType(), genTO.getProperties().get(0).getReturnType());
                } else if (genTO.getName().equals("BitsLong")) {
                    assertEquals(1, genTO.getProperties().size());
                    assertEquals(Types.primitiveLongType(), genTO.getProperties().get(0).getReturnType());
                    bitsLongFound = true;
                } else if (genTO.getName().equals("BitsIntArray")) {
                    bitsIntArrayFound = true;
                    assertEquals(Types.intArrayType(), genTO.getProperties().get(0).getReturnType());
                }
            }
        }
        assertTrue(bitsIntFound);
        assertTrue(bitsLongFound);
        assertTrue(bitsIntArrayFound);
    }

}
