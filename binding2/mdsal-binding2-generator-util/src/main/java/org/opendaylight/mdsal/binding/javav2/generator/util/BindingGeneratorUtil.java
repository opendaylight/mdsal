/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Standard Util class that contains various method for converting
 * input strings to valid JAVA language strings e.g. package names,
 * class names, attribute names and/or valid JavaDoc comments.
 */
@Beta
public final class BindingGeneratorUtil {

    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');
    private static final char UNDERSCORE = '_';
    private static final char DASH = '-';
    private static final int FIRST_CHAR = 0;

    private BindingGeneratorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Normalizing package name according to
     * <a href="https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html">Naming a
     * Package in JAVA</a>.
     * In binding generator v2 flow, this method is called after dealing with possible non-Java
     * characters.
     *
     * @param name
     *            - part of package name
     * @return normalized name
     */
    public static String normalizePackageName(final String name) {
        final StringBuilder normalizedName = new StringBuilder(name);
        if (BindingMapping.JAVA_RESERVED_WORDS.contains(name)) {
            return normalizedName.append(UNDERSCORE).toString();
        }
        final char firstChar = name.charAt(FIRST_CHAR);
        if (!Character.isJavaIdentifierStart(firstChar) || Character.isDigit(firstChar)) {
            normalizedName.insert(FIRST_CHAR, UNDERSCORE);
        }
        if (name.contains(String.valueOf(DASH))) {
            return normalizedName.toString().replaceAll(String.valueOf(DASH), String.valueOf(UNDERSCORE));
        }
        return normalizedName.toString();
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
     *
     * @param basePackageName
     *            string with package name of the module, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        final Iterable<QName> pathTowardsRoot = schemaPath.getPathTowardsRoot();
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        final int size = Iterables.size(pathTowardsRoot) - 1;
        if (size <= 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, pathFromRoot, size);
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code> which crosses an augmentation.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
     *
     * @param basePackageName
     *            string with package name of the module, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForAugmentedGeneratedType(final String basePackageName, final SchemaPath schemaPath) {
        final Iterable<QName> pathTowardsRoot = schemaPath.getPathTowardsRoot();
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        final int size = Iterables.size(pathTowardsRoot);
        if (size == 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, pathFromRoot, size);
    }


    private static String generateNormalizedPackageName(final String base, final Iterable<QName> path, final int size) {
        final StringBuilder builder = new StringBuilder(base);
        final Iterator<QName> iterator = path.iterator();
        for (int i = 0; i < size; ++i) {
            builder.append('.');
            final String nodeLocalName = iterator.next().getLocalName();
            //FIXME: colon or dash in identifier?
            builder.append(nodeLocalName);
        }
        return BindingMapping.normalizePackageName(builder.toString());
    }

    /**
     * Encodes angle brackets in yang statement description
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(final String description) {
        String newDesc = description;
        if (newDesc != null) {
            newDesc = LT_MATCHER.replaceFrom(newDesc, "&lt;");
            newDesc = GT_MATCHER.replaceFrom(newDesc, "&gt;");
        }
        return newDesc;
    }

    //TODO: further implementation of static util methods...
}
