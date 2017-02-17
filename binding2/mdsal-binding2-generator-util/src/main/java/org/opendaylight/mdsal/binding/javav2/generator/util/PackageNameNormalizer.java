/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

public final class PackageNameNormalizer {

    private static final char UNDERSCORE = '_';
    private static final int FIRST_CHAR = 0;
    private static final char DASH = '-';

    private PackageNameNormalizer() {
        throw new UnsupportedOperationException("Util class.");
    }

    /**
     * Normalizing package name according to
     * <a href="https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html">Naming a
     * Package in JAVA</a>
     *
     * @param name
     *            - part of package name
     * @return normalized name
     */
    public static String packageNameNormalizer(final String name) {
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
}
