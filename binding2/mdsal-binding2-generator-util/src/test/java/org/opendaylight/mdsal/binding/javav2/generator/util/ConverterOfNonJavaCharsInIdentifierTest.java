/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConverterOfNonJavaCharsInIdentifierTest {

    @Test
    public void acceptableCharsEnumTest() throws Exception {
        assertTest("Acceptable", "Acceptable", JavaTypeOfIdentifier.ENUM);
    }

    /**
     * Is the same test for class and interface identifiers
     */
    @Test
    public void nonAcceptableCharsEnumTest() throws Exception {
        assertTest("acceptable", "Acceptable", JavaTypeOfIdentifier.ENUM);
        assertTest("acc*", "AccAsterisk", JavaTypeOfIdentifier.ENUM);
        assertTest("Acc*", "AccAsterisk", JavaTypeOfIdentifier.ENUM);
        assertTest("*acc", "AsteriskAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("*Acc", "AsteriskAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("*", "Asterisk", JavaTypeOfIdentifier.ENUM);
        assertTest("\\acc", "ReverseSolidusAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("\\Acc", "ReverseSolidusAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("\\", "ReverseSolidus", JavaTypeOfIdentifier.ENUM);
        assertTest("/acc", "SolidusAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("/Acc", "SolidusAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("/", "Solidus", JavaTypeOfIdentifier.ENUM);
        assertTest("1acc", "DigitOneAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("1Acc", "DigitOneAcc", JavaTypeOfIdentifier.ENUM);
        assertTest("acc1", "Acc1", JavaTypeOfIdentifier.ENUM);
        assertTest("Acc1", "Acc1", JavaTypeOfIdentifier.ENUM);
        assertTest("1", "DigitOne", JavaTypeOfIdentifier.ENUM);
        assertTest("%", "PercentSign", JavaTypeOfIdentifier.ENUM);
    }

    @Test
    public void acceptableCharsEnumValueTest() throws Exception {
        assertTest("ACCEPTABLE", "ACCEPTABLE", JavaTypeOfIdentifier.ENUM_VALUE);
    }

    @Test
    public void nonAcceptableCharsEnumValueTest() throws Exception {
        assertTest("acceptable", "ACCEPTABLE", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("Acceptable", "ACCEPTABLE", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("acc*", "ACCASTERISK", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("Acc*", "ACCASTERISK", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("*acc", "ASTERISKACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("*Acc", "ASTERISKACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("*", "ASTERISK", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("\\acc", "REVERSESOLIDUSACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("\\Acc", "REVERSESOLIDUSACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("\\", "REVERSESOLIDUS", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("/acc", "SOLIDUSACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("/Acc", "SOLIDUSACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("/", "SOLIDUS", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("1acc", "DIGITONEACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("1Acc", "DIGITONEACC", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("acc1", "ACC1", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("Acc1", "ACC1", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("1", "DIGITONE", JavaTypeOfIdentifier.ENUM_VALUE);
        assertTest("%", "PERCENTSIGN", JavaTypeOfIdentifier.ENUM_VALUE);
    }

    @Test
    public void acceptableCharsMethodeTest() throws Exception {
        assertTest("acceptable", "acceptable", JavaTypeOfIdentifier.METHODE);
    }

    /**
     * Is the same test for variables identifiers
     */
    @Test
    public void nonAcceptableCharsMethodeTest() throws Exception {
        assertTest("acc*", "accAsterisk", JavaTypeOfIdentifier.METHODE);
        assertTest("Acc*", "accAsterisk", JavaTypeOfIdentifier.METHODE);
        assertTest("*acc", "asteriskAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("*Acc", "asteriskAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("*", "asterisk", JavaTypeOfIdentifier.METHODE);
        assertTest("\\acc", "reverseSolidusAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("\\Acc", "reverseSolidusAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("\\", "reverseSolidus", JavaTypeOfIdentifier.METHODE);
        assertTest("/acc", "solidusAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("/Acc", "solidusAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("/", "solidus", JavaTypeOfIdentifier.METHODE);
        assertTest("1acc", "digitOneAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("1Acc", "digitOneAcc", JavaTypeOfIdentifier.METHODE);
        assertTest("acc1", "acc1", JavaTypeOfIdentifier.METHODE);
        assertTest("Acc1", "acc1", JavaTypeOfIdentifier.METHODE);
        assertTest("1", "digitOne", JavaTypeOfIdentifier.METHODE);
        assertTest("%", "percentSign", JavaTypeOfIdentifier.METHODE);
    }

    private void assertTest(final String testedIdentifier, final String acceptable,
            final JavaTypeOfIdentifier javaTypeOfIdentifier) {
        final String convertedIdentifier = ConverterOfNonJavaCharsInIdentifier
                .findAndConvertAllNonJavaCharsInIdentifier(testedIdentifier, javaTypeOfIdentifier);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertEquals(acceptable, convertedIdentifier);
    }
}
