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
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.Revision;
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

    public static final String MODULE_INFO_CLASS_NAME = "$YangModuleInfoImpl";
    public static final String MODEL_BINDING_PROVIDER_CLASS_NAME = "$YangModelBindingProvider";
    public static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";
    public static final String MEMBER_PATTERN_LIST = "patterns";
    public static final String MEMBER_REGEX_LIST = "regexes";
    public static final String RPC_INPUT_SUFFIX = "Input";
    public static final String RPC_OUTPUT_SUFFIX = "Output";

    private static final Pattern COLON_SLASH_SLASH = Pattern.compile("://", Pattern.LITERAL);
    private static final String QUOTED_DOT = Matcher.quoteReplacement(".");
    private static final String NEGATED_PATTERN_PREFIX = "^(?!";
    private static final String NEGATED_PATTERN_SUFFIX = ").*$";

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

        final Optional<Revision> optRev = module.getRevision();
        if (optRev.isPresent()) {
            // Revision is in format 2017-10-26, we want the output to be 171026, which is a matter of picking the
            // right characters.
            final String rev = optRev.get().toString();
            checkArgument(rev.length() == 10, "Unsupported revision %s", rev);
            packageNameBuilder.append("rev").append(rev, 2, 4).append(rev, 5, 7).append(rev.substring(8));
        } else {
            // No-revision packages are special
            packageNameBuilder.append("norev");
        }
        return packageNameBuilder.toString();
    }

    /**
     * Create a {@link Pattern} expression which performs inverted match to the specified pattern. The input pattern
     * is expected to be a valid regular expression passing {@link Pattern#compile(String)} and to have both start and
     * end of string anchors as the first and last characters.
     *
     * @param pattern Pattern regular expression to negate
     * @return Negated regular expression
     * @throws IllegalArgumentException if the pattern does not conform to expected structure
     * @throws NullPointerException if pattern is null
     */
    public static String negatePatternString(final String pattern) {
        checkArgument(pattern.charAt(0) == '^' && pattern.charAt(pattern.length() - 1) == '$',
                "Pattern '%s' does not have expected format", pattern);

        /*
         * Converting the expression into a negation is tricky. For example, when we have:
         *
         *   pattern "a|b" { modifier invert-match; }
         *
         * this gets escaped into either "^a|b$" or "^(?:a|b)$". Either format can occur, as the non-capturing group
         * strictly needed only in some cases. From that we want to arrive at:
         *   "^(?!(?:a|b)$).*$".
         *
         *           ^^^         original expression
         *        ^^^^^^^^       tail of a grouped expression (without head anchor)
         *    ^^^^        ^^^^   inversion of match
         *
         * Inversion works by explicitly anchoring at the start of the string and then:
         * - specifying a negative lookahead until the end of string
         * - matching any string
         * - anchoring at the end of the string
         */
        final boolean hasGroup = pattern.startsWith("^(?:") && pattern.endsWith(")$");
        final int len = pattern.length();
        final StringBuilder sb = new StringBuilder(len + (hasGroup ? 7 : 11)).append(NEGATED_PATTERN_PREFIX);

        if (hasGroup) {
            sb.append(pattern, 1, len);
        } else {
            sb.append("(?:").append(pattern, 1, len - 1).append(")$");
        }
        return sb.append(NEGATED_PATTERN_SUFFIX).toString();
    }

    /**
     * Check if the specified {@link Pattern} is the result of {@link #negatePatternString(String)}. This method
     * assumes the pattern was not hand-coded but rather was automatically-generated, such that its non-automated
     * parts come from XSD regular expressions. If this constraint is violated, this method may result false positives.
     *
     * @param pattern Pattern to check
     * @return True if this pattern is a negation.
     * @throws NullPointerException if pattern is null
     * @throws IllegalArgumentException if the pattern does not conform to expected structure
     */
    public static boolean isNegatedPattern(final Pattern pattern) {
        return isNegatedPattern(pattern.toString());
    }

    private static boolean isNegatedPattern(final String pattern) {
        return pattern.startsWith(NEGATED_PATTERN_PREFIX) && pattern.endsWith(NEGATED_PATTERN_SUFFIX);
    }

    //TODO: further implementation of static util methods...

}
