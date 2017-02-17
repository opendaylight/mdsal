/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

public class BindingGeneratorUtilTest {

    private static final char UNDERSCORE = '_';

    @Test
    public void resrvedKeyWordsTest() {
        for (final String reservedKeyword : BindingMapping.JAVA_RESERVED_WORDS) {
            final String packageNameNormalizer = BindingGeneratorUtil.normalizePackageName(reservedKeyword);
            final StringBuilder expected = new StringBuilder(reservedKeyword).append(UNDERSCORE);
            assertEquals(expected.toString(), packageNameNormalizer);
        }
    }

    @Test
    public void digitAtStartTest() {
        for (int i = 0; i < 10; i++) {
            final String str_i = String.valueOf(i);
            final String packageNameNormalizer = BindingGeneratorUtil.normalizePackageName(str_i);
            final StringBuilder expected = new StringBuilder(str_i).insert(0, UNDERSCORE);
            assertEquals(expected.toString(), packageNameNormalizer);
        }
    }

    @Test
    public void dashTest() {
        final String test = "str-test";
        final String packageNameNormalizer = BindingGeneratorUtil.normalizePackageName(test);
        final String expected = "str_test";
        assertEquals(expected, packageNameNormalizer);
    }

    @Test
    public void nonJavaStartChar() {
        testNonJavaChar("*foo", "_*foo");
        testNonJavaChar("//foo", "_//foo");
        testNonJavaChar("\foo", "_\foo");
        testNonJavaChar(":foo", "_:foo");
    }

    private void testNonJavaChar(final String tested, final Object expected) {
        final String packageNameNormalizer = BindingGeneratorUtil.normalizePackageName(tested);
        assertEquals(expected, packageNameNormalizer);
    }

}
