/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

public final class ConverterOfNonJavaCharsOfIdentifier {

    private final static int JAVA_IDENTIFIER_START = 0;

    private ConverterOfNonJavaCharsOfIdentifier() {
        throw new UnsupportedOperationException("Util class");
    }

    public static String findAndConvertAllNonJavaCharsInIdentifier(final String identifier) {
        final StringBuilder identifierAfterConverted = new StringBuilder();
        final char charAtStartOfIdentifier = identifier.charAt(JAVA_IDENTIFIER_START);

        prepareActualChar(identifierAfterConverted, charAtStartOfIdentifier,
                Character.isJavaIdentifierStart(charAtStartOfIdentifier));

        for (int i = 1; i < identifier.length(); i++) {
            prepareActualChar(identifierAfterConverted, charAtStartOfIdentifier,
                    Character.isJavaIdentifierPart(identifier.charAt(i)));
        }
        return identifierAfterConverted.toString();
    }

    private static void prepareActualChar(final StringBuilder identifierAfterConverted,
            final char charAtStartOfIdentifier, final boolean isJavaIdentifierChar) {
        if (!isJavaIdentifierChar) {
            identifierAfterConverted.append(transformToAcceptableString(charAtStartOfIdentifier));
        } else {
            identifierAfterConverted.append(charAtStartOfIdentifier);
        }
    }

    /**
     * @param charAtStartOfIdentifier
     * @return
     */
    private static Object transformToAcceptableString(final char charAtStartOfIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

}
