/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Standard Util class that provides generated Java related functionality
 */
@Beta
public final class BindingMapping {

    public static final Set<String> JAVA_RESERVED_WORDS = ImmutableSet.of("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "double", "do", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while");

    public static final Set<String> WINDOWS_RESERVED_WORDS = ImmutableSet.of("CON", "PRN", "AUX", "CLOCK$", "NUL",
            "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT0", "LPT1", "LPT2",
            "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    public static final String QNAME_STATIC_FIELD_NAME = "QNAME";

    /**
     * Package prefix for Binding v2 generated Java code structures
     */
    public static final String PACKAGE_PREFIX = "org.opendaylight.mdsal.gen.javav2";

    private static final Splitter CAMEL_SPLITTER = Splitter.on(CharMatcher.anyOf(" _.-/").precomputed())
            .omitEmptyStrings().trimResults();
    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");
    public static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";
    public static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";
    public static final String MEMBER_PATTERN_LIST = "patterns";

    private static final ThreadLocal<SimpleDateFormat> PACKAGE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyMMdd");
        }

        @Override
        public void set(final SimpleDateFormat value) {
            throw new UnsupportedOperationException();
        }
    };

    private BindingMapping() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getRootPackageName(final Module module) {
        checkArgument(module != null, "Module must not be null");
        checkArgument(module.getRevision() != null, "Revision must not be null");
        checkArgument(module.getNamespace() != null, "Namespace must not be null");

        final StringBuilder packageNameBuilder = new StringBuilder();
        packageNameBuilder.append(PACKAGE_PREFIX);
        packageNameBuilder.append('.');

        String namespace = module.getNamespace().toString();
        namespace = COLON_SLASH_SLASH.matcher(namespace).replaceAll(QUOTED_DOT);

        final char[] chars = namespace.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            switch (chars[i]) {
                case '/':
                case ':':
                case '-':
                case '@':
                case '$':
                case '#':
                case '\'':
                case '*':
                case '+':
                case ',':
                case ';':
                case '=':
                    chars[i] = '.';
                    break;
                default:
                    // no-op, any other character is kept as it is
            }
        }

        packageNameBuilder.append(chars);
        if (chars[chars.length - 1] != '.') {
            packageNameBuilder.append('.');
        }

        //TODO: per yangtools dev, semantic version not used yet
//        final SemVer semVer = module.getSemanticVersion();
//        if (semVer != null) {
//            packageNameBuilder.append(semVer.toString());
//        } else {
//            packageNameBuilder.append("rev");
//            packageNameBuilder.append(PACKAGE_DATE_FORMAT.get().format(module.getRevision()));
//        }

        packageNameBuilder.append("rev");
        packageNameBuilder.append(PACKAGE_DATE_FORMAT.get().format(module.getRevision()));
        return packageNameBuilder.toString();
    }

    /**
     * Prepares valid Java class name
     * @param localName
     * @return class name
     */
    public static String getClassName(final String localName) {
        checkArgument(localName != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(localName));
    }

    public static String getClassName(final QName name) {
        checkArgument(name != null, "Name should not be null.");
        return toFirstUpper(toCamelCase(name.getLocalName()));
    }

    private static String toCamelCase(final String rawString) {
        checkArgument(rawString != null, "String should not be null");
        final Iterable<String> components = CAMEL_SPLITTER.split(rawString);
        final StringBuilder builder = new StringBuilder();
        for (final String comp : components) {
            builder.append(toFirstUpper(comp));
        }
        return checkNumericPrefix(builder.toString());
    }

    private static String checkNumericPrefix(final String rawString) {
        if ((rawString == null) || rawString.isEmpty()) {
            return rawString;
        }
        if (Character.isDigit(rawString.charAt(0))) {
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

    /**
     * Prepares Java property name for method getter code generation
     * @param yangIdentifier given YANG element local name
     * @return property name
     */
    public static String getPropertyName(final String yangIdentifier) {
        final String potential = toFirstLower(toCamelCase(yangIdentifier));
        if ("class".equals(potential)) {
            return "xmlClass";
        }
        return potential;
    }

    /**
     * Returns the {@link String} {@code s} with an
     * {@link Character#isLowerCase(char) lower case} first character. This
     * function is null-safe.
     *
     * @param s
     *            the string that should get an lower case first character. May
     *            be <code>null</code>.
     * @return the {@link String} {@code s} with an lower case first character
     *         or <code>null</code> if the input {@link String} {@code s} was
     *         <code>null</code>.
     */
    private static String toFirstLower(final String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        if (s.length() == 1) {
            return s.toLowerCase();
        }
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    //TODO: further implementation of static util methods...

}
