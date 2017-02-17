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
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

public class NonJavaCharsConverterTest {

    @Test
    public void acceptableCharsEnumTest() throws Exception {
        assertTest("Acceptable", "Acceptable", JavaIdentifier.ENUM);
    }

    /**
     * Is the same test for class and interface identifiers
     */
    @Test
    public void nonAcceptableCharsEnumTest() throws Exception {
        assertTest("acceptable", "Acceptable", JavaIdentifier.ENUM);
        assertTest("acc*", "AccAsterisk", JavaIdentifier.ENUM);
        assertTest("Acc*", "AccAsterisk", JavaIdentifier.ENUM);
        assertTest("Acc*acc", "AccAsteriskAcc", JavaIdentifier.ENUM);
        assertTest("*acc", "AsteriskAcc", JavaIdentifier.ENUM);
        assertTest("*Acc", "AsteriskAcc", JavaIdentifier.ENUM);
        assertTest("*", "Asterisk", JavaIdentifier.ENUM);
        assertTest("\\acc", "ReverseSolidusAcc", JavaIdentifier.ENUM);
        assertTest("\\Acc", "ReverseSolidusAcc", JavaIdentifier.ENUM);
        assertTest("\\", "ReverseSolidus", JavaIdentifier.ENUM);
        assertTest("/acc", "SolidusAcc", JavaIdentifier.ENUM);
        assertTest("/Acc", "SolidusAcc", JavaIdentifier.ENUM);
        assertTest("/", "Solidus", JavaIdentifier.ENUM);
        assertTest("1acc", "DigitOneAcc", JavaIdentifier.ENUM);
        assertTest("1Acc", "DigitOneAcc", JavaIdentifier.ENUM);
        assertTest("acc1", "Acc1", JavaIdentifier.ENUM);
        assertTest("Acc1", "Acc1", JavaIdentifier.ENUM);
        assertTest("1", "DigitOne", JavaIdentifier.ENUM);
        assertTest("%", "PercentSign", JavaIdentifier.ENUM);
        assertTest("foo-bar", "FooBar", JavaIdentifier.ENUM);
        assertTest("foo--bar", "FooHyphenMinusHyphenMinusBar", JavaIdentifier.ENUM);
        assertTest("-foo", "HyphenMinusFoo", JavaIdentifier.ENUM);
        assertTest("--foo", "HyphenMinusHyphenMinusFoo", JavaIdentifier.ENUM);
        assertTest("foo-", "FooHyphenMinus", JavaIdentifier.ENUM);
        assertTest("foo--", "FooHyphenMinusHyphenMinus", JavaIdentifier.ENUM);
        assertTest("-foo-", "HyphenMinusFooHyphenMinus", JavaIdentifier.ENUM);
        assertTest("-foo-bar-", "HyphenMinusFooBarHyphenMinus", JavaIdentifier.ENUM);
        assertTest("-foo--bar-", "HyphenMinusFooHyphenMinusHyphenMinusBarHyphenMinus", JavaIdentifier.ENUM);
    }

    @Test
    public void acceptableCharsEnumValueTest() throws Exception {
        assertTest("ACCEPTABLE", "ACCEPTABLE", JavaIdentifier.ENUM_VALUE);
    }

    @Test
    public void nonAcceptableCharsEnumValueTest() throws Exception {
        assertTest("acceptable", "ACCEPTABLE", JavaIdentifier.ENUM_VALUE);
        assertTest("Acceptable", "ACCEPTABLE", JavaIdentifier.ENUM_VALUE);
        assertTest("Acce_ptable", "ACCE_PTABLE", JavaIdentifier.ENUM_VALUE);
        assertTest("acc*", "ACC_ASTERISK", JavaIdentifier.ENUM_VALUE);
        assertTest("Acc*", "ACC_ASTERISK", JavaIdentifier.ENUM_VALUE);
        assertTest("*acc", "ASTERISK_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("*Acc", "ASTERISK_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("*", "ASTERISK", JavaIdentifier.ENUM_VALUE);
        assertTest("\\acc", "REVERSE_SOLIDUS_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("\\Acc", "REVERSE_SOLIDUS_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("\\", "REVERSE_SOLIDUS", JavaIdentifier.ENUM_VALUE);
        assertTest("/acc", "SOLIDUS_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("/Acc", "SOLIDUS_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("/", "SOLIDUS", JavaIdentifier.ENUM_VALUE);
        assertTest("1acc", "DIGIT_ONE_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("1Acc", "DIGIT_ONE_ACC", JavaIdentifier.ENUM_VALUE);
        assertTest("acc1", "ACC1", JavaIdentifier.ENUM_VALUE);
        assertTest("Acc1", "ACC1", JavaIdentifier.ENUM_VALUE);
        assertTest("1", "DIGIT_ONE", JavaIdentifier.ENUM_VALUE);
        assertTest("%", "PERCENT_SIGN", JavaIdentifier.ENUM_VALUE);
        assertTest("foo-bar", "FOO_BAR", JavaIdentifier.ENUM_VALUE);
        assertTest("foo--bar", "FOO_HYPHEN_MINUS_HYPHEN_MINUS_BAR", JavaIdentifier.ENUM_VALUE);
        assertTest("-foo", "HYPHEN_MINUS_FOO", JavaIdentifier.ENUM_VALUE);
        assertTest("--foo", "HYPHEN_MINUS_HYPHEN_MINUS_FOO", JavaIdentifier.ENUM_VALUE);
        assertTest("foo-", "FOO_HYPHEN_MINUS", JavaIdentifier.ENUM_VALUE);
        assertTest("foo--", "FOO_HYPHEN_MINUS_HYPHEN_MINUS", JavaIdentifier.ENUM_VALUE);
        assertTest("-foo-", "HYPHEN_MINUS_FOO_HYPHEN_MINUS", JavaIdentifier.ENUM_VALUE);
        assertTest("-foo-bar-", "HYPHEN_MINUS_FOO_BAR_HYPHEN_MINUS", JavaIdentifier.ENUM_VALUE);
        assertTest("-foo--bar-", "HYPHEN_MINUS_FOO_HYPHEN_MINUS_HYPHEN_MINUS_BAR_HYPHEN_MINUS",
                JavaIdentifier.ENUM_VALUE);
    }

    @Test
    public void acceptableCharsMethodTest() throws Exception {
        assertTest("acceptable", "acceptable", JavaIdentifier.METHOD);
    }

    /**
     * Is the same test for variables identifiers
     */
    @Test
    public void nonAcceptableCharsMethodTest() throws Exception {
        assertTest("acc*", "accAsterisk", JavaIdentifier.METHOD);
        assertTest("Acc*", "accAsterisk", JavaIdentifier.METHOD);
        assertTest("*acc", "asteriskAcc", JavaIdentifier.METHOD);
        assertTest("*Acc", "asteriskAcc", JavaIdentifier.METHOD);
        assertTest("*", "asterisk", JavaIdentifier.METHOD);
        assertTest("\\acc", "reverseSolidusAcc", JavaIdentifier.METHOD);
        assertTest("\\Acc", "reverseSolidusAcc", JavaIdentifier.METHOD);
        assertTest("\\", "reverseSolidus", JavaIdentifier.METHOD);
        assertTest("/acc", "solidusAcc", JavaIdentifier.METHOD);
        assertTest("/Acc", "solidusAcc", JavaIdentifier.METHOD);
        assertTest("/", "solidus", JavaIdentifier.METHOD);
        assertTest("1acc", "digitOneAcc", JavaIdentifier.METHOD);
        assertTest("1Acc", "digitOneAcc", JavaIdentifier.METHOD);
        assertTest("acc1", "acc1", JavaIdentifier.METHOD);
        assertTest("Acc1", "acc1", JavaIdentifier.METHOD);
        assertTest("1", "digitOne", JavaIdentifier.METHOD);
        assertTest("%", "percentSign", JavaIdentifier.METHOD);
        assertTest("foo-bar", "fooBar", JavaIdentifier.METHOD);
        assertTest("foo--bar", "fooHyphenMinusHyphenMinusBar", JavaIdentifier.METHOD);
        assertTest("-foo", "hyphenMinusFoo", JavaIdentifier.METHOD);
        assertTest("--foo", "hyphenMinusHyphenMinusFoo", JavaIdentifier.METHOD);
        assertTest("foo-", "fooHyphenMinus", JavaIdentifier.METHOD);
        assertTest("foo--", "fooHyphenMinusHyphenMinus", JavaIdentifier.METHOD);
        assertTest("-foo-", "hyphenMinusFooHyphenMinus", JavaIdentifier.METHOD);
        assertTest("-foo-bar-", "hyphenMinusFooBarHyphenMinus", JavaIdentifier.METHOD);
        assertTest("-foo--bar-", "hyphenMinusFooHyphenMinusHyphenMinusBarHyphenMinus", JavaIdentifier.METHOD);
    }


    @Test
    public void resrvedKeyWordsTest() {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            testReservedKeyword(reservedKeyword);
        }
        for (final String reservedKeyword : BindingMapping.WINDOWS_RESERVED_WORDS) {
            testReservedKeyword(reservedKeyword);
        }
    }

    private void testReservedKeyword(final String reservedKeyword) {
        final String packageNameNormalizer = NonJavaCharsConverter.normalizePackageName(reservedKeyword);
        final StringBuilder expected = new StringBuilder(reservedKeyword).append('_');
        assertEquals(expected.toString(), packageNameNormalizer);
    }

    @Test
    public void digitAtStartTest() {
        for (int i = 0; i < 10; i++) {
            final String str_i = String.valueOf(i);
            final String packageNameNormalizer = NonJavaCharsConverter.normalizePackageName(str_i);
            final String expected = Character.getName(str_i.charAt(0)).replaceAll(" ", "").toLowerCase();
            assertEquals(expected.toString(), packageNameNormalizer);
        }
    }

    @Test
    public void dashTest() {
        dashTest("foo-bar", "foo_bar");
        dashTest("foo--bar", "foo__bar");
        dashTest("-foo", "_foo");
        dashTest("--foo", "__foo");
        dashTest("foo-", "foo_");
        dashTest("foo--", "foo__");
        dashTest("-foo-bar", "_foo_bar");
        dashTest("foo-bar-", "foo_bar_");
        dashTest("-foo-bar-", "_foo_bar_");
        dashTest("-foo--bar-", "_foo__bar_");
    }

    private void dashTest(final String tested, final String expected) {
        final String actual = NonJavaCharsConverter.normalizePackageName(tested);
        assertEquals(expected, actual);
    }

    @Test
    public void nonJavaStartChar() {
        testNonJavaChar("*foo", "asteriskfoo");
        testNonJavaChar("/foo", "solidusfoo");
        testNonJavaChar("\\foo", "reversesolidusfoo");
        testNonJavaChar(":foo", "colonfoo");

        testNonJavaChar("f*oo", "fasteriskoo");
        testNonJavaChar("f/oo", "fsolidusoo");
        testNonJavaChar("f\\oo", "freversesolidusoo");
        testNonJavaChar("f:oo", "fcolonoo");

        testNonJavaChar("foo*", "fooasterisk");
        testNonJavaChar("foo/", "foosolidus");
        testNonJavaChar("foo\\", "fooreversesolidus");
        testNonJavaChar("foo:", "foocolon");

        testNonJavaChar("_foo_", "_foo_");
        testNonJavaChar("f_oo", "f_oo");

    }

    private void testNonJavaChar(final String tested, final Object expected) {
        final String packageNameNormalizer = NonJavaCharsConverter.normalizePackageName(tested);
        assertEquals(expected, packageNameNormalizer);
    }

    private void assertTest(final String testedIdentifier, final String acceptable,
            final JavaIdentifier javaTypeOfIdentifier) {
        final String convertedIdentifier =
                NonJavaCharsConverter.convertIdentifier(testedIdentifier, javaTypeOfIdentifier);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertEquals(acceptable, convertedIdentifier);
    }
}
