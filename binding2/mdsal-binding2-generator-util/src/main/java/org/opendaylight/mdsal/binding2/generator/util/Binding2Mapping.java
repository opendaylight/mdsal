/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Standard Util class that provides generated Java related functionality
 */
@Beta
public final class Binding2Mapping {

    private Binding2Mapping() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final Set<String> JAVA_RESERVED_WORDS = ImmutableSet.of("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "double", "do", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while");

    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";
    public static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final String PACKAGE_PREFIX = "org.opendaylight.yang.gen.v2";
    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Interner<String> PACKAGE_INTERNER = Interners.newWeakInterner();
    private static final Splitter CAMEL_SPLITTER = Splitter.on(CharMatcher.anyOf(" _.-/").precomputed())
            .omitEmptyStrings().trimResults();

    public static String normalizePackageName(final String packageName) {
        if (packageName == null) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String p : DOT_SPLITTER.split(packageName.toLowerCase())) {
            if (first) {
                first = false;
            } else {
                builder.append('.');
            }

            //FIXME: don't use underscore in v2
            if (Character.isDigit(p.charAt(0)) || Binding2Mapping.JAVA_RESERVED_WORDS.contains(p)) {
                builder.append('_');
            }
            builder.append(p);
        }

        // Prevent duplication of input string
        return PACKAGE_INTERNER.intern(builder.toString());
    }

    public static String getClassName(final String localName) {
        checkArgument(localName != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(localName));
    }

    private static String toCamelCase(final String rawString) {
        checkArgument(rawString != null, "String should not be null");
        Iterable<String> components = CAMEL_SPLITTER.split(rawString);
        StringBuilder builder = new StringBuilder();
        for (String comp : components) {
            builder.append(toFirstUpper(comp));
        }
        return checkNumericPrefix(builder.toString());
    }

    private static String checkNumericPrefix(final String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return rawString;
        }
        char firstChar = rawString.charAt(0);
        if (firstChar >= '0' && firstChar <= '9') {
            return "_" + rawString;
        } else {
            return rawString;
        }
    }

    /**
     * Returns the {@link String} {@code s} with an
     * {@link Character#isUpperCase(char) upper case} first character. This
     * function is null-safe.
     *
     * @param s
     *            the string that should get an upper case first character. May
     *            be <code>null</code>.
     * @return the {@link String} {@code s} with an upper case first character
     *         or <code>null</code> if the input {@link String} {@code s} was
     *         <code>null</code>.
     */
    public static String toFirstUpper(final String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String getRootPackageName(final QName module) {
        return getRootPackageName(module.getModule());
    }

    public static String getRootPackageName(final QNameModule module) {
        //FIX ME implement this
        return null;
    }

    //TODO: further implementation of static util methods...

}
