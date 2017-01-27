/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConverterOfNonJavaCharsInIdentifierTest {

    @Test
    public void acceptableCharsTest() throws Exception {
        final String convertedIdentifier =
                ConverterOfNonJavaCharsInIdentifier.findAndConvertAllNonJavaCharsInIdentifier("Acceptable",
                        JavaTypeOfIdentifier.ENUM);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertTrue(convertedIdentifier.equals("Acceptable"));
    }

    @Test
    public void nonAcceptableCharsTest() throws Exception {
        String convertedIdentifier = ConverterOfNonJavaCharsInIdentifier
                .findAndConvertAllNonJavaCharsInIdentifier("Acc*", JavaTypeOfIdentifier.ENUM);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertTrue(convertedIdentifier.equals("AccAsterisk"));

        convertedIdentifier = ConverterOfNonJavaCharsInIdentifier
                .findAndConvertAllNonJavaCharsInIdentifier("*Acc", JavaTypeOfIdentifier.ENUM);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertTrue(convertedIdentifier.equals("AsteriskAcc"));
    }
}
