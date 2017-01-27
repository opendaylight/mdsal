/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

public final class NonJavaCharsConverter {

    private final static int FIRST_CHAR = 0;

    private NonJavaCharsConverter() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Find and convert non Java chars in identifiers of generated transfer
     * objects, initially derived from corresponding YANG.
     *
     * @See <a>http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8</a>
     *
     * @param identifier
     *            - name of identifier
     * @param javaIdentifier
     *            - java type of identifier
     * @return - java acceptable identifier
     */

    public static String convertIdentifier(final String identifier, final JavaIdentifier javaIdentifier) {
        final StringBuilder sb = new StringBuilder();
        final char firstChar = identifier.charAt(FIRST_CHAR);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            sb.append(convertFirst(firstChar, existNext(identifier, FIRST_CHAR)));
        } else {
            sb.append(firstChar);
        }

        for (int i = 1; i < identifier.length(); i++) {
            final char actualChar = identifier.charAt(i);
            if (!Character.isJavaIdentifierPart(actualChar)) {
                final String convertedLastChar = sb.toString();
                sb.append(convert(actualChar, existNext(identifier, i),
                        convertedLastChar.charAt(convertedLastChar.length() - 1)));
            } else {
                sb.append(actualChar);
            }
        }
        return fixCasesByJavaType(sb.toString().toLowerCase(), javaIdentifier);
    }

    /**
     * @param string
     * @param javaIdentifier
     * @return
     */
    private static String fixCasesByJavaType(final String convertedIdentifier, final JavaIdentifier javaIdentifier) {
        switch (javaIdentifier) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                return capitalize(fixCases(convertedIdentifier));
            case ENUM_VALUE:
                return convertedIdentifier.toUpperCase();
            case METHOD:
            case VARIABLE:
                return fixCases(convertedIdentifier);
            default:
                throw new IllegalArgumentException("Unknown java type of identifier : " + javaIdentifier.toString());
        }
    }

    /**
     * @param convertedIdentifier
     * @return
     */
    private static String fixCases(final String convertedIdentifier) {
        final StringBuilder sb = new StringBuilder();
        if (convertedIdentifier.contains("_")) {
            boolean isFirst = true;
            for (final String part : convertedIdentifier.split("_")) {
                if (isFirst) {
                    isFirst = false;
                    sb.append(part);
                } else {
                    sb.append(capitalize(part));
                }
            }
        } else {
            sb.append(convertedIdentifier);
        }
        return sb.toString();
    }

    private static boolean existNext(final String identifier, final int actual) {
        if ((identifier.length() - 1) < (actual + 1)) {
            return false;
        } else {
            return true;
        }
    }

    private static String convertFirst(final char c, final boolean existNext) {
        String name = Character.getName(c);
        name = existNext ? (name + "_") : name;
        return name.contains(" ") ? name.replaceAll(" ", "_") : name;
    }

    private static String convert(final char c, final boolean existNext, final char convertedLastChar) {
        return convertedLastChar == '_' ? convertFirst(c, existNext) : "_" + convertFirst(c, existNext);
    }

    private static String capitalize(final String identifier) {
        return identifier.substring(FIRST_CHAR, FIRST_CHAR + 1).toUpperCase() + identifier.substring(1);
    }

}
