/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.generator.impl.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding2.generator.util.Types;
import org.opendaylight.mdsal.binding2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Util class
 */
public final class YangTextTemplate {
    private static final CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t");
    private static final String DOT = ".";
    private static final String COMMA = ",";
    private static final char NEW_LINE = '\n';
    private static final CharMatcher NL_MATCHER = CharMatcher.is(NEW_LINE);
    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Splitter NL_SPLITTER = Splitter.on(NL_MATCHER);
    private static final CharMatcher TAB_MATCHER = CharMatcher.is('\t');
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

    private YangTextTemplate() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Used in #yangtemplateformodule.scala.txt for formating revision description
     *
     * @param text Content of tag description
     * @param nextLineIndent Number of spaces from left side default is 12
     * @return formatted description
     */
    public static String formatToParagraph(final String text, final int nextLineIndent) {
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

    /**
     * Used in all yangtemplates for formating augmentation target
     *
     * @param schemaPath path to augmented node
     * @return path in string format
     */
    public static String formatToAugmentPath(final Iterable<QName> schemaPath) {
        final StringBuilder sb = new StringBuilder();
        for (QName pathElement : schemaPath) {
            sb.append("\\(")
            .append(pathElement.getNamespace())
            .append(')')
            .append(pathElement.getLocalName());
        }
        return sb.toString();
    }

    /**
     * Evaluates if it is necessary to add the package name for type to the map of imports for parentGenType
     * If it is so the package name is saved to the map imports.
     *
     * @param parentGenType generated type for which is the map of necessary imports build
     * @param type JAVA type for which is the necessary of the package import evaluated
     * @param imports map of the imports for parentGenType
     */
    public static void putTypeIntoImports(final GeneratedType parentGenType, final Type type,
        final Map<String, String> imports) {
        checkArgument(parentGenType != null, "Parent Generated Type parameter MUST be specified and cannot be NULL!");
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(parentGenType.getPackageName() != null,
                "Parent Generated Type cannot have Package Name referenced as NULL!");

        final String typeName = Preconditions.checkNotNull(type.getName());
        final String typePackageName = Preconditions.checkNotNull(type.getPackageName());
        final String parentTypeName = Preconditions.checkNotNull(parentGenType.getName());
        if (typeName.equals(parentTypeName) || typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return;
        }
        if (!imports.containsKey(typeName)) {
            imports.put(typeName, typePackageName);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            final Type[] params = paramType.getActualTypeArguments();
            if (params != null) {
                for (Type param : params) {
                    putTypeIntoImports(parentGenType, param, imports);
                }
            }
        }
    }

    /**
     * Builds the string which contains either the full path to the type (package name with type) or only type name
     * if the package is among imports.
     *
     * @param parentGenType generated type which contains type
     * @param type JAVA type for which is the string with type info generated
     * @param imports map of necessary imports for parentGenType
     * @return string with type name for type in the full format or in the short format
     */
    public static String getExplicitType(final GeneratedType parentGenType, final Type type,
        final Map<String, String> imports) {
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(imports != null, "Imports Map cannot be NULL!");

        final String typePackageName = Preconditions.checkNotNull(type.getPackageName());
        final String typeName = Preconditions.checkNotNull(type.getName());
        final String importedPackageName = imports.get(typeName);
        final StringBuilder builder;
        if (typePackageName.equals(importedPackageName)) {
            builder = new StringBuilder(typeName);
            addActualTypeParameters(builder, type, parentGenType, imports);
            if (builder.toString().equals("Void")) {
                return "void";
            }
        } else {
            builder = new StringBuilder();
            if (!typePackageName.isEmpty()) {
                builder.append(typePackageName + DOT + typeName);
            } else {
                builder.append(type.getName());
            }
            if (type.equals(Types.voidType())) {
                return "void";
            }
            addActualTypeParameters(builder, type, parentGenType, imports);
        }
        return builder.toString();
    }

    /**
     * Adds actual type parameters from type to builder if type is ParametrizedType.
     *
     * @param builder string builder which contains type name
     * @param type JAVA Type for which is the string with type info generated
     * @param parentGenType generated type which contains type
     * @param imports map of necessary imports for parentGenType
     * @return adds actual type parameters to builder
     */
    private static StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type,
        final GeneratedType parentGenType, final Map<String, String> imports) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            builder.append("<");
            builder.append(getParameters(parentGenType, pTypes, imports));
            builder.append(">");
        }
        return builder;
    }

    /**
     * Generates the string with all actual type parameters from
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param pTypes array of Type instances = actual type parameters
     * @param availableImports map of imports for parentGenType
     * @return string with all actual type parameters from pTypes
     */
    private static String getParameters(final GeneratedType parentGenType, final Type[] pTypes,
        final Map<String, String> availableImports) {
        if (pTypes == null || pTypes.length == 0) {
            return "?";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pTypes.length; i++) {
            final Type t = pTypes[i];

            String separator = COMMA;
            if (i == (pTypes.length - 1)) {
                separator = "";
            }

            String wildcardParam = "";
            if (t.equals(Types.voidType())) {
                builder.append("java.lang.Void")
                .append(separator);
                continue;
            } else {

                if (t instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }

                builder.append(wildcardParam)
                .append(getExplicitType(parentGenType, t, availableImports) + separator);
            }
        }
        return builder.toString();
    }

    /**
     * Wraps text as documentation
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
            sb.append(" *");
            if (t.isEmpty()) {
                sb.append(" ");
                sb.append(t);
            }
            sb.append(NEW_LINE);
        }
        sb.append(" */");
        return sb.toString();
    }

    public static String encodeJavadocSymbols(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }
        String ret = description.replace("*/", "&#42;&#47;");
        return AMP_MATCHER.replaceFrom(ret, "&amp;");
    }
}