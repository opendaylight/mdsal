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
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

public class JavaIdentifierNormalizerTest {

    @Test
    public void specialPathsTest() {
        ModuleContext context = new ModuleContext();
        // QName - reserved & non reserved
        String normalizeIdentifier =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.opendaylight.yangtools.yang.common",
                        "QName", context);
        assertEquals("QName", normalizeIdentifier);
        // again reserved
        normalizeIdentifier =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.opendaylight.yangtools.yang.common",
                        "QName", context);
        assertEquals("QName", normalizeIdentifier);
        // non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("qname.non.reserved", "QName", context);
        assertEquals("Qname", normalizeIdentifier);
        // again non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("qname.non.reserved", "QName", context);
        assertEquals("Qname1", normalizeIdentifier);

        // Augmentable - reserved & non reserved
        normalizeIdentifier = JavaIdentifierNormalizer
                .normalizeClassIdentifier("org.opendaylight.mdsal.binding.javav2.spec.structural", "Augmentable", context);
        assertEquals("Augmentable", normalizeIdentifier);
        // again reserved
        normalizeIdentifier = JavaIdentifierNormalizer
                .normalizeClassIdentifier("org.opendaylight.mdsal.binding.javav2.spec.structural", "Augmentable", context);
        assertEquals("Augmentable", normalizeIdentifier);
        // non reserved
        normalizeIdentifier = JavaIdentifierNormalizer
                .normalizeClassIdentifier("augmentable.non.reserved", "Augmentable", context);
        assertEquals("Augmentable", normalizeIdentifier);
        // again non reserved
        normalizeIdentifier =
                JavaIdentifierNormalizer.normalizeClassIdentifier("augmentable.non.reserved", "Augmentable", context);
        assertEquals("Augmentable1", normalizeIdentifier);

        // List - reserved & non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("java.util", "List", context);
        assertEquals("List", normalizeIdentifier);
        // again reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("java.util", "List", context);
        assertEquals("List", normalizeIdentifier);
        // non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("list.non.reserved", "List", context);
        assertEquals("List", normalizeIdentifier);
        // again non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("list.non.reserved", "List", context);
        assertEquals("List1", normalizeIdentifier);

        // String - reserved & non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("java.lang", "String", context);
        assertEquals("String", normalizeIdentifier);
        // again reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("java.lang", "String", context);
        assertEquals("String", normalizeIdentifier);
        // non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("string.non.reserved", "String", context);
        assertEquals("String", normalizeIdentifier);
        // again non reserved
        normalizeIdentifier = JavaIdentifierNormalizer.normalizeClassIdentifier("string.non.reserved", "String", context);
        assertEquals("String1", normalizeIdentifier);
    }

    /**
     * This test tests normalizing of enum and interface identifiers too.
     */
    @Test
    public void sameClassNamesSamePackageTest() {
        ModuleContext context = new ModuleContext();
        String normalizeIdentifier1 = JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.same.package",
                "Foo", context);
        String normalizeIdentifier2 = JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.same.package",
                "fOo", context);
        final String normalizeIdentifier3 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.same.package", "foo", context);
        assertEquals(normalizeIdentifier1, "Foo");
        assertEquals(normalizeIdentifier2, "Foo1");
        assertEquals(normalizeIdentifier3, "Foo2");

        normalizeIdentifier1 = JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.same.package", "*",
                context);
        normalizeIdentifier2 = JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.same.package",
                "asterisk", context);
        assertEquals(normalizeIdentifier1, "Asterisk");
        assertEquals(normalizeIdentifier2, "Asterisk1");
    }

    /**
     * This test tests normalizing of enum and interface identifiers too.
     */
    @Test
    public void sameClassNamesOtherPackageTest() {
        ModuleContext context = new ModuleContext();
        String normalizeIdentifier1 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.other.package", "Foo", context);
        String normalizeIdentifier2 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.other.package.next", "fOo", context);
        final String normalizeIdentifier3 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.other.package.next.next", "foo", context);
        assertEquals(normalizeIdentifier1, "Foo");
        assertEquals(normalizeIdentifier2, "Foo");
        assertEquals(normalizeIdentifier3, "Foo");

        normalizeIdentifier1 = JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.other.package", "*",
                context);
        normalizeIdentifier2 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.other.package.next", "asterisk", context);
        assertEquals(normalizeIdentifier1, "Asterisk");
        assertEquals(normalizeIdentifier2, "Asterisk");
    }

    /**
     * This test tests normalizing of enum and interface identifiers too.
     */
    @Test
    public void sameClassNamesSamePackageReservedWordsTest() {
        ModuleContext context = new ModuleContext();
        final String normalizeIdentifier1 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.same.package", "int", context);
        final String normalizeIdentifier2 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.same.package", "InT", context);
        final String normalizeIdentifier3 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.same.package", "inT", context);
        assertEquals(normalizeIdentifier1, "IntReservedKeyword");
        assertEquals(normalizeIdentifier2, "IntReservedKeyword1");
        assertEquals(normalizeIdentifier3, "IntReservedKeyword2");
    }

    /**
     * This test tests normalizing of enum and interface identifiers too.
     */
    @Test
    public void sameClassNamesOtherPackageReservedWordsTest() {
        ModuleContext context = new ModuleContext();
        final String normalizeIdentifier1 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.other.package", "int", context);
        final String normalizeIdentifier2 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.other.package.next", "InT", context);
        final String normalizeIdentifier3 =
                JavaIdentifierNormalizer.normalizeClassIdentifier("org.example.keywords.other.package.next.next",
                        "inT", context);
        assertEquals(normalizeIdentifier1, "IntReservedKeyword");
        assertEquals(normalizeIdentifier2, "IntReservedKeyword");
        assertEquals(normalizeIdentifier3, "IntReservedKeyword");
    }

    /**
     * This test tests converting of reserved keywords like class and interface identifiers too.
     */
    @Test
    public void identifierReservedKeyWordsEnumConverterTest() throws Exception {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            testIdentifierReservedKeywordEnum(reservedKeyword);
        }
        for (final String reservedKeyword : BindingMapping.WINDOWS_RESERVED_WORDS) {
            testIdentifierReservedKeywordEnum(reservedKeyword);
        }
        testIdentifierReservedKeywordEnum("iNt");
        testIdentifierReservedKeywordEnum("cOn");
    }

    private void testIdentifierReservedKeywordEnum(final String reservedKeyword) {
        final StringBuilder expected =
                new StringBuilder(reservedKeyword.substring(0, 1).toUpperCase())
                        .append(reservedKeyword.substring(1).toLowerCase())
                        .append("ReservedKeyword");
        assertTest(reservedKeyword, expected.toString(), JavaIdentifier.ENUM);
    }

    /**
     * This test tests converting of reserved keywords like constant identifier too.
     */
    @Test
    public void identifierReservedKeyWordsEnumValueConverterTest() throws Exception {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            testIdentifierReservedKeywordEnumValue(reservedKeyword);
        }
        for (final String reservedKeyword : BindingMapping.WINDOWS_RESERVED_WORDS) {
            testIdentifierReservedKeywordEnumValue(reservedKeyword);
        }
        testIdentifierReservedKeywordEnumValue("iNt");
        testIdentifierReservedKeywordEnumValue("cOn");
    }

    private void testIdentifierReservedKeywordEnumValue(final String reservedKeyword) {
        final StringBuilder expected = new StringBuilder(reservedKeyword.toUpperCase()).append("_RESERVED_KEYWORD");
        assertTest(reservedKeyword, expected.toString(), JavaIdentifier.ENUM_VALUE);
    }

    /**
     * This test tests converting of reserved keywords like variable identifier too.
     */
    @Test
    public void identifierReservedKeyWordsMethodConverterTest() throws Exception {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            testIdentifierReservedKeywordMethod(reservedKeyword);
        }
        for (final String reservedKeyword : BindingMapping.WINDOWS_RESERVED_WORDS) {
            testIdentifierReservedKeywordMethod(reservedKeyword);
        }
        testIdentifierReservedKeywordMethod("iNt");
        testIdentifierReservedKeywordMethod("cOn");
    }

    private void testIdentifierReservedKeywordMethod(final String reservedKeyword) {
        final StringBuilder expected = new StringBuilder(reservedKeyword.toLowerCase()).append("ReservedKeyword");
        assertTest(reservedKeyword, expected.toString(), JavaIdentifier.METHOD);
    }

    @Test
    public void acceptableCharsEnumTest() throws Exception {
        assertTest("Acceptable", "Acceptable", JavaIdentifier.ENUM);
    }

    /**
     * This test tests converting of class and interface identifiers too.
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
        assertTest("foo.", "FooFullStop", JavaIdentifier.ENUM);
        assertTest(".foo", "FullStopFoo", JavaIdentifier.ENUM);
        assertTest("bar.foo", "BarFullStopFoo", JavaIdentifier.ENUM);
    }

    @Test
    public void acceptableCharsEnumValueTest() throws Exception {
        assertTest("ACCEPTABLE", "ACCEPTABLE", JavaIdentifier.ENUM_VALUE);
    }

    /**
     * This test tests converting of constant identifier too.
     */
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
        assertTest("foo.", "FOO_FULL_STOP", JavaIdentifier.ENUM_VALUE);
        assertTest(".foo", "FULL_STOP_FOO", JavaIdentifier.ENUM_VALUE);
        assertTest("bar.foo", "BAR_FULL_STOP_FOO", JavaIdentifier.ENUM_VALUE);
    }

    @Test
    public void acceptableCharsMethodTest() throws Exception {
        assertTest("acceptable", "acceptable", JavaIdentifier.METHOD);
    }

    /**
     * This test tests converting of variable identifier too.
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
        assertTest("foo.", "fooFullStop", JavaIdentifier.METHOD);
        assertTest(".foo", "fullStopFoo", JavaIdentifier.METHOD);
        assertTest("foo.bar", "fooFullStopBar", JavaIdentifier.METHOD);
    }


    @Test
    public void packageReservedKeyWordsTest() {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            testPackageReservedKeyword(reservedKeyword);
        }
        for (final String reservedKeyword : BindingMapping.WINDOWS_RESERVED_WORDS) {
            testPackageReservedKeyword(reservedKeyword);
        }
    }

    private void testPackageReservedKeyword(final String reservedKeyword) {
        final String packageNameNormalizer = JavaIdentifierNormalizer.normalizePartialPackageName(reservedKeyword);
        final StringBuilder expected = new StringBuilder(reservedKeyword).append('_');
        assertEquals(expected.toString().toLowerCase(), packageNameNormalizer);
    }

    @Test
    public void digitAtStartTest() {
        for (int i = 0; i < 10; i++) {
            final String str_i = String.valueOf(i);
            final String packageNameNormalizer = JavaIdentifierNormalizer.normalizePartialPackageName(str_i);
            final String expected = Character.getName(str_i.charAt(0)).replaceAll(" ", "").toLowerCase();
            assertEquals(expected.toString(), packageNameNormalizer);
        }
    }

    @Test
    public void normalizePackageNameDashTest() {
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
        final String actual = JavaIdentifierNormalizer.normalizePartialPackageName(tested);
        assertEquals(expected, actual);
    }

    @Test
    public void normalizePackageNameTest() {
        normalizePackageNameTest("*foo", "asteriskfoo");
        normalizePackageNameTest("/foo", "solidusfoo");
        normalizePackageNameTest("\\foo", "reversesolidusfoo");
        normalizePackageNameTest(":foo", "colonfoo");

        normalizePackageNameTest("f*oo", "fasteriskoo");
        normalizePackageNameTest("f/oo", "fsolidusoo");
        normalizePackageNameTest("f\\oo", "freversesolidusoo");
        normalizePackageNameTest("f:oo", "fcolonoo");

        normalizePackageNameTest("foo*", "fooasterisk");
        normalizePackageNameTest("foo/", "foosolidus");
        normalizePackageNameTest("foo\\", "fooreversesolidus");
        normalizePackageNameTest("foo:", "foocolon");

        normalizePackageNameTest("_foo_", "_foo_");
        normalizePackageNameTest("f_oo", "f_oo");
    }

    private void normalizePackageNameTest(final String tested, final Object expected) {
        final String packageNameNormalizer = JavaIdentifierNormalizer.normalizePartialPackageName(tested);
        assertEquals(expected, packageNameNormalizer);
    }

    private void assertTest(final String testedIdentifier, final String acceptable,
            final JavaIdentifier javaTypeOfIdentifier) {
        final String convertedIdentifier =
                JavaIdentifierNormalizer.normalizeSpecificIdentifier(testedIdentifier, javaTypeOfIdentifier);
        assertNotNull(convertedIdentifier);
        assertTrue(!convertedIdentifier.isEmpty());
        assertEquals(acceptable, convertedIdentifier);
    }

    @Test
    public void realPackageNameExampleTest() {
        String tested = "org.opendaylight.example.test.rev000000.data.foo";
        String expected = "org.opendaylight.example.test.rev000000.data.foo";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo*";
        expected = "org.opendaylight.example.test.rev000000.data.fooasterisk";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo*bar";
        expected = "org.opendaylight.example.test.rev000000.data.fooasteriskbar";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo.bar";
        expected = "org.opendaylight.example.test.rev000000.data.foo.bar";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo-bar";
        expected = "org.opendaylight.example.test.rev000000.data.foo_bar";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo_";
        expected = "org.opendaylight.example.test.rev000000.data.foo_";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data._foo";
        expected = "org.opendaylight.example.test.rev000000.data._foo";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.foo*.*";
        expected = "org.opendaylight.example.test.rev000000.data.fooasterisk.asterisk";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.int";
        expected = "org.opendaylight.example.test.rev000000.data.int_";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.iNt";
        expected = "org.opendaylight.example.test.rev000000.data.int_";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.con";
        expected = "org.opendaylight.example.test.rev000000.data.con_";
        testRealPackageNameExample(tested, expected);

        tested = "org.opendaylight.example.test.rev000000.data.CON";
        expected = "org.opendaylight.example.test.rev000000.data.con_";
        testRealPackageNameExample(tested, expected);
    }

    private void testRealPackageNameExample(final String tested, final String expected) {
        final String actual = JavaIdentifierNormalizer.normalizeFullPackageName(tested);
        assertEquals(expected, actual);
    }
}
