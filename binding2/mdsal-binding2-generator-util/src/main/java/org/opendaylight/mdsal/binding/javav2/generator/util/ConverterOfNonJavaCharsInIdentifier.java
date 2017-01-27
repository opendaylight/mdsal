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

    /**
     * Converting yang chars of identifiers for java chars of identifiers by
     * <a>http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8</a>
     *
     * @param identifier
     *            - name of identifier
     * @param javaTypeOfIdentifier
     *            - java type of identifier
     * @return - java acceptable identifier
     */
    public static String findAndConvertAllNonJavaCharsInIdentifier(final String identifier,
            final JavaTypeOfIdentifier javaTypeOfIdentifier) {
        final StringBuilder identifierAfterConverted = new StringBuilder();
        final char charAtStartOfIdentifier = identifier.charAt(JAVA_IDENTIFIER_START);
        boolean isStart = true;
        boolean isJavaIdentifierAtStart = Character.isJavaIdentifierStart(charAtStartOfIdentifier);
        checkAndWriteActualChar(identifierAfterConverted, charAtStartOfIdentifier, javaTypeOfIdentifier,
                isJavaIdentifierAtStart, isStart, false);
        isStart = false;
        for (int i = 1; i < identifier.length(); i++) {
            final char actualChar = identifier.charAt(i);
            if (!Character.isJavaIdentifierPart(identifier.charAt(i - 1)) || !isJavaIdentifierAtStart) {
                isStart = true;
                isJavaIdentifierAtStart = true;
            }
            checkAndWriteActualChar(identifierAfterConverted, actualChar, javaTypeOfIdentifier,
                    Character.isJavaIdentifierPart(actualChar), isStart, isJavaIdentifierAtStart);
            isStart = false;
        }
        return identifierAfterConverted.toString();
    }

    private static void checkAndWriteActualChar(final StringBuilder identifierAfterConverted,
            final char actualCharOfIdentifier, final JavaTypeOfIdentifier javaTypeOfIdentifier,
            final boolean isJavaIdentifierChar, final boolean isStartOfIdentifier,
            final boolean isJavaIdentifierAtStart) {
        if (!isJavaIdentifierChar) {
            identifierAfterConverted
                    .append(transformToAcceptableNameOfIdentifier(actualCharOfIdentifier, javaTypeOfIdentifier,
                            isStartOfIdentifier, isJavaIdentifierAtStart));
        } else {
            identifierAfterConverted.append(makeNameAcceptableByJavaType(actualCharOfIdentifier + "",
                    javaTypeOfIdentifier, isStartOfIdentifier, isJavaIdentifierAtStart));
        }
    }

    private static String transformToAcceptableNameOfIdentifier(final char actualCharOfIdentifier,
            final JavaTypeOfIdentifier javaTypeOfIdentifier, final boolean isStartOfIdentifier,
            final boolean isJavaIdentifierAtStart) {
        final String nameOfCharByUnicode = Character.getName(actualCharOfIdentifier);
        if(nameOfCharByUnicode.contains(" ")) {
            final StringBuilder nameOfCharByUnicodeWithoutWs = new StringBuilder();
            boolean isFirstPartOfName = true;
            boolean upperCaseOfMethodAndVariables = false;
            for (final String partOfName : nameOfCharByUnicode.split("\\s+")) {
                nameOfCharByUnicodeWithoutWs.append(makeNameAcceptableByJavaType(partOfName, javaTypeOfIdentifier,
                        isStartOfIdentifier, upperCaseOfMethodAndVariables));
                if (isFirstPartOfName) {
                    upperCaseOfMethodAndVariables = true;
                    isFirstPartOfName = false;
                }
            }
            return nameOfCharByUnicodeWithoutWs.toString();
        } else {
            return makeNameAcceptableByJavaType(nameOfCharByUnicode, javaTypeOfIdentifier, isStartOfIdentifier,
                    isJavaIdentifierAtStart);
        }
    }

    private static String makeNameAcceptableByJavaType(final String partOfName,
            final JavaTypeOfIdentifier javaTypeOfIdentifier, final boolean isStartOfIdentifier,
            final boolean isJavaIdentifierAtStart) {
        switch (javaTypeOfIdentifier) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                return capitalize(partOfName, isStartOfIdentifier);
            case ENUM_VALUE:
                return partOfName.toUpperCase();
            case METHODE:
            case VARIABLE:
                if (isStartOfIdentifier && !isJavaIdentifierAtStart) {
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
