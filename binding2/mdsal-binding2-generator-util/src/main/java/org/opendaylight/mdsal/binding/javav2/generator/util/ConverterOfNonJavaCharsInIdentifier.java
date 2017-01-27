/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

public final class ConverterOfNonJavaCharsInIdentifier {

    private final static int JAVA_IDENTIFIER_START = 0;

    private ConverterOfNonJavaCharsInIdentifier() {
        throw new UnsupportedOperationException("Util class");
    }

    public static String findAndConvertAllNonJavaCharsInIdentifier(final String identifier,
            final JavaTypeOfIdentifier javaTypeOfIdentifier) {
        final StringBuilder identifierAfterConverted = new StringBuilder();
        final char charAtStartOfIdentifier = identifier.charAt(JAVA_IDENTIFIER_START);

        checkAndWriteActualChar(identifierAfterConverted, charAtStartOfIdentifier, javaTypeOfIdentifier,
                Character.isJavaIdentifierStart(charAtStartOfIdentifier), true);

        for (int i = 1; i < identifier.length(); i++) {
            final char actualChar = identifier.charAt(i);
            checkAndWriteActualChar(identifierAfterConverted, actualChar, javaTypeOfIdentifier,
                    Character.isJavaIdentifierPart(actualChar), false);
        }
        return identifierAfterConverted.toString();
    }

    private static void checkAndWriteActualChar(final StringBuilder identifierAfterConverted,
            final char actualCharOfIdentifier, final JavaTypeOfIdentifier javaTypeOfIdentifier,
            final boolean isJavaIdentifierChar, final boolean isStartOfIdentifier) {
        if (!isJavaIdentifierChar) {
            identifierAfterConverted
                    .append(transformToAcceptableNameOfIdentifier(actualCharOfIdentifier, javaTypeOfIdentifier,
                            isStartOfIdentifier));
        } else {
            identifierAfterConverted.append(actualCharOfIdentifier);
        }
    }

    private static String transformToAcceptableNameOfIdentifier(final char actualCharOfIdentifier,
            final JavaTypeOfIdentifier javaTypeOfIdentifier, final boolean isStartOfIdentifier) {
        final String nameOfCharByUnicode = Character.getName(actualCharOfIdentifier);
        if(nameOfCharByUnicode.contains(" ")) {
            final StringBuilder nameOfCharByUnicodeWithoutWs = new StringBuilder();
            for (final String partOfName : nameOfCharByUnicode.split("\\s+")) {
                nameOfCharByUnicodeWithoutWs
                        .append(makeNameAcceptableByJavaType(partOfName, javaTypeOfIdentifier, isStartOfIdentifier));
            }
            return nameOfCharByUnicodeWithoutWs.toString();
        } else {
            return makeNameAcceptableByJavaType(nameOfCharByUnicode, javaTypeOfIdentifier, isStartOfIdentifier);
        }
    }

    private static String makeNameAcceptableByJavaType(final String partOfName,
            final JavaTypeOfIdentifier javaTypeOfIdentifier, final boolean isStartOfIdentifier) {
        switch (javaTypeOfIdentifier) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                return capitalize(partOfName, isStartOfIdentifier);
            case ENUM_VALUE:
                return partOfName;
            case METHODE:
            case VARIABLE:
                if(isStartOfIdentifier) {
                    return partOfName.toLowerCase();
                } else {
                    return capitalize(partOfName, isStartOfIdentifier);
                }
            default:
                throw new IllegalArgumentException(
                        "Unknown java type of identifier : " + javaTypeOfIdentifier.toString());
        }
    }

    private static String capitalize(final String partOfName, final boolean isStartOfIdentifier) {
        final String base = partOfName.charAt(JAVA_IDENTIFIER_START) + "";
        if ((partOfName.length() <= 1)) {
            if(isStartOfIdentifier) {
                return base.toUpperCase();
            } else {
                return base.toLowerCase();
            }
        } else {
            return base.toUpperCase()
                    + partOfName.substring(1).toLowerCase();
        }
    }

}
