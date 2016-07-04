/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.java.api.generator.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.TypeMember;
import org.opendaylight.yangtools.concepts.Builder;

public class TextTemplateUtil {
    private Map<String, String> importMap = new HashMap<String,String>();
    private Set<String> setOfImports;

    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final String COMMA = ",";
    private static final String UNDERSCORE = "_";
    private static final String TO_STRING = ".toString";
    private static final char NEW_LINE = '\n';
    private static final CharMatcher NL_MATCHER = CharMatcher.is(NEW_LINE);
    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Splitter NL_SPLITTER = Splitter.on(NL_MATCHER);
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');
    public static final String DOT = ".";
    public static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";

    private TextTemplateUtil() {
        throw new AssertionError("Instantiating utility class.");
    }

    /**
     * Makes start of getter name LowerCase
     *
     * @param s getter name without prefix
     * @return getter name starting in LowerCase
     */
    public static String toFirstLower(String s) {
        return s != null && s.length() != 0?(Character.isLowerCase(s.charAt(0))?s:(s.length() == 1?s.toLowerCase():s.substring(0, 1).toLowerCase() + s.substring(1))):s;
    }

    /**
     * Wraps text as documentation, used in enum description
     *
     * @param text text for wrapping
     * @return wrapped text
     */
    public static String wrapToDocumentation(String text) {
        if (text.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder("/**");
        sb.append(NEW_LINE);
        Iterable<String> lineSplitText = NL_SPLITTER.split(text);
        for (final String t : lineSplitText) {
            if (!t.isEmpty()) {
                sb.append(" *");
                sb.append(" ");
                sb.append(t);
                sb.append(NEW_LINE);
            }
        }
        sb.append(" */");
        return sb.toString();
    }

    public static String formatDataForJavaDocBuilder(String typeName) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Class that builds {@link ")
                .append(typeName)
                .append("} instances.")
                .append(NEW_LINE)
                .append("@see ")
                .append(typeName);
        return stringBuilder.toString();
    }

    public static String formatDataForJavaDoc(GeneratedType type) {
        final String description = type.getDescription().isPresent() ? type.getDescription().get() : "";
        return encodeJavadocSymbols(description);
    }

    /**
     * Template method which generates JAVA comments. InterfaceTemplate
     *
     * @param comment comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    public static String asJavadoc(String comment) {
        if (comment == null) {
            return "";
        }
        return wrapToDocumentation(formatToParagraph(comment.trim(), 0));
    }

    private static String formatDataForJavaDoc(TypeMember type, String additionalComment) {
        StringBuilder javaDoc = new StringBuilder();
        if (type.getComment() != null || !type.getComment().isEmpty()) {
            javaDoc.append(formatToParagraph(type.getComment(), 0))
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }
        javaDoc.append(additionalComment);
        return wrapToDocumentation(javaDoc.toString());
    }

    public static String getJavaDocForInterface(MethodSignature methodSignature) {
        StringBuilder javaDoc = new StringBuilder();
        javaDoc.append("@return ")
                .append(asCode(methodSignature.getReturnType().getFullyQualifiedName()))
                .append(" ")
                .append(asCode(propertyNameFromGetter(methodSignature)))
                .append(", or ")
                .append(asCode("null"))
                .append(" if not present");
        return formatDataForJavaDoc(methodSignature, javaDoc.toString());
    }

    private static String asCode(String text) {
        return "<code>" + text + "</code>";
    }

    /**
     * Encodes angle brackets in yang statement description
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(String description) {
        if (description != null) {
            description = LT_MATCHER.replaceFrom(description, "&lt;");
            description = GT_MATCHER.replaceFrom(description, "&gt;");
        }
        return description;
    }

    /**
     * sets fieldname according to property for return type
     * method(type fieldname)
     *
     * @param property type from getter
     */
    public static String fieldName(GeneratedProperty property) {
        final String name = Preconditions.checkNotNull(property.getName());
        return UNDERSCORE.concat(name);
    }

    /**
     * Helper method for building getter
     *
     * @param field property name
     * @return getter for propery
     */
    public static String getterMethodName (GeneratedProperty field) {
        final Type type = Preconditions.checkNotNull(field.getReturnType());
        final String name = Preconditions.checkNotNull(field.getName());
        final String prefix = Types.BOOLEAN.equals(type) ? "is" : "get";
        return prefix.concat(toFirstUpper(name));
    }

    public static String getSimpleNameForBuilder() {
        return Builder.class.getSimpleName();
    }

    /**
     * Makes start of getter name uppercase
     *
     * @param s getter name without prefix
     * @return getter name starting in uppercase
     */
    public static String toFirstUpper(String s) {
        return s != null && s.length() != 0?(Character.isUpperCase(s.charAt(0))?s:(s.length() == 1?s.toUpperCase():s.substring(0, 1).toUpperCase() + s.substring(1))):s;
    }

    /**
     * Cuts prefix from getter name
     *
     * @param getter getter name
     * @return getter name without prefix
     */
    public static String propertyNameFromGetter(MethodSignature getter) {
        final String name = Preconditions.checkNotNull(getter.getName());
        int prefix;
        if (name.startsWith("is")) {
            prefix = 2;
        } else if (name.startsWith("get")) {
            prefix = 3;
        } else {
            throw new IllegalArgumentException("Not a getter");
        }
        return toFirstLower(name.substring(prefix));
    }

    public static String getPropertyList(List<GeneratedProperty> properties) {
        final List<String> strings = new LinkedList<>();
        if (!properties.isEmpty()) {
            for (GeneratedProperty property : properties) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("base.")
                        .append(getterMethodName(property))
                        .append("()");
                strings.add(stringBuilder.toString());
            }
        }
        return String.join(", ", strings);
    }

    /**
     * @param text Content of tag description
     * @param nextLineIndent Number of spaces from left side default is 12
     * @return formatted description
     */
    private static String formatToParagraph(final String text, final int nextLineIndent) {
        if (Strings.isNullOrEmpty(text)) {
            return "";
        }
        boolean isFirstElementOnNewLineEmptyChar = false;
        final StringBuilder sb = new StringBuilder();
        final StringBuilder lineBuilder = new StringBuilder();
        final String lineIndent = Strings.repeat(" ", nextLineIndent);
        final String textToFormat = NEWLINE_OR_TAB.removeFrom(text);
        final String formattedText = textToFormat.replaceAll(" +", " ");
        final StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements()) {
            final String nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                // Trim trailing whitespace
                for (int i = lineBuilder.length() - 1; i >= 0 && lineBuilder.charAt(i) != ' '; --i) {
                    lineBuilder.setLength(i);
                }
                // Trim leading whitespace
                while (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.deleteCharAt(0);
                }
                sb.append(lineBuilder).append('\n');
                lineBuilder.setLength(0);

                if (nextLineIndent > 0) {
                    sb.append(lineIndent);
                }

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = true;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = false;
            } else {
                lineBuilder.append(nextElement);
            }
        }
        return sb.append(lineBuilder).append('\n').toString();
    }

    private static String encodeJavadocSymbols(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }
        final String ret = description.replace("*/", "&#42;&#47;");
        return AMP_MATCHER.replaceFrom(ret, "&amp;");
    }
}