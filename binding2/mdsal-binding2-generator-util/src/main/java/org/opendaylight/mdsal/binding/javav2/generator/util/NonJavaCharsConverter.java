/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;

/**
 * This util class converts every non-java char in identifier to java char by its unicode name
 * (<a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8">JAVA SE
 * SPEFICIATIONS - Identifiers</a>). There are special types of mapping non-java chars to original
 * identifiers according to specific {@linkplain JavaIdentifier java type}:
 * <ul>
 * <li>class, enum, interface</li>
 * <li>
 * <ul>
 * <li>without special separator</li>
 * <li>the first character of identifier, any other first character of identifier part mapped by
 * non-Java char name from unicode and char in identifier behind non-java char name are converting
 * to upper case</li>
 * <li>examples:</li>
 * <li>
 * <ul>
 * <li>example* - ExampleAsterisk</li>
 * <li>example*example - ExampleAserisksExample</li>
 * <li>\example - ReverseSolidusExample</li>
 * <li>1example - DigitOneExample</li>
 * <li>example1 - Example1</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>enum value, constant</li>
 * <li>
 * <ul>
 * <li>used underscore as special separator</li>
 * <li>converted identifier to upper case</li>
 * <li>examples:</li>
 * <li>
 * <ul>
 * <li>example* - EXAMPLE_ASTERISK</li>
 * <li>example*example - EXAMPLE_ASTERISK_EXAMPLE</li>
 * <li>\example - REVERSE_SOLIDUS_EXAMPLE</li>
 * <li>1example - DIGIT_ONE_EXAMPLE</li>
 * <li>example1 - EXAMPLE1</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>method, variable
 * <li>
 * <li>
 * <ul>
 * <li>without special separator</li>
 * <li>the first character of identifier is converting to lower case</li>
 * <li>any other first character of identifier part mapped by non-Java char name from unicode and
 * char in identifier behind non-java char name are converting to upper case
 * <li>examples:</li>
 * <li>
 * <ul>
 * <li>example* - exampleAsterisk</li>
 * <li>example*example - exampleAserisksExample</li>
 * <li>\example - reverseSolidusExample</li>
 * <li>1example - digitOneExample</li>
 * <li>example1 - example1</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 */
@Beta
public final class NonJavaCharsConverter {

    private final static int FIRST_CHAR = 0;

    private NonJavaCharsConverter() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Find and convert non Java chars in identifiers of generated transfer
     * objects, initially derived from corresponding YANG.
     *
     * <a>http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8</a>
     *
     * @param identifier
     *            - name of identifier
     * @param javaIdentifier
     *            - java type of identifier
     * @return - java acceptable identifier
     */
    public static String convertIdentifier(final String identifier, final JavaIdentifier javaIdentifier) {
        final StringBuilder sb = new StringBuilder();

        // check and convert first char in identifier if there is non-java char
        final char firstChar = identifier.charAt(FIRST_CHAR);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            // converting first char of identifier
            sb.append(convertFirst(firstChar, existNext(identifier, FIRST_CHAR)));
        } else {
            sb.append(firstChar);
        }
        // check and convert other chars in identifier, if there is non-java char
        for (int i = 1; i < identifier.length(); i++) {
            final char actualChar = identifier.charAt(i);
            if (!Character.isJavaIdentifierPart(actualChar)) {
                // prepare actual string of sb for checking if underscore exist on position of the
                // last char
                final String partialConvertedIdentifier = sb.toString();
                sb.append(convert(actualChar, existNext(identifier, i),
                        partialConvertedIdentifier.charAt(partialConvertedIdentifier.length() - 1)));
            } else {
                sb.append(actualChar);
            }
        }
        // apply camel case in appropriate way
        return fixCasesByJavaType(sb.toString().toLowerCase(), javaIdentifier);
    }

    /**
     * Fix cases of converted identifiers by Java type
     *
     * @param string
     *            - converted identifier
     * @param javaIdentifier
     *            - java type of identifier
     * @return converted identifier with right cases according to java type
     */
    private static String fixCasesByJavaType(final String convertedIdentifier, final JavaIdentifier javaIdentifier) {
        switch (javaIdentifier) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                return capitalize(fixCases(convertedIdentifier));
            case ENUM_VALUE:
            case CONSTANT:
                return convertedIdentifier.toUpperCase();
            case METHOD:
            case VARIABLE:
                return fixCases(convertedIdentifier);
            default:
                throw new IllegalArgumentException("Unknown java type of identifier : " + javaIdentifier.toString());
        }
    }

    /**
     * Delete unnecessary chars in converted identifier and apply camel case in appropriate way.
     *
     * @param convertedIdentifier
     *            - original converted identifier
     * @return resolved identifier
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

    /**
     * Check if there exist next char in identifier behind actual char position
     *
     * @param identifier
     *            - original identifier
     * @param actual
     *            - actual char position
     * @return true if there is another char, false otherwise
     */
    private static boolean existNext(final String identifier, final int actual) {
      return (identifier.length() - 1) < (actual + 1) ? false : true;
    }

    /**
     * Converting first char of identifier. This happen only if this char is
     * non-java char
     *
     * @param c
     *            - first char
     * @param existNext
     *            - existing of next char behind actual char
     * @return converted char
     */
    private static String convertFirst(final char c, final boolean existNext) {
        String name = Character.getName(c);
        name = existNext ? (name + "_") : name;
        return name.contains(" ") ? name.replaceAll(" ", "_") : name;
    }

    /**
     * Converting any char in java identifier, This happen only if this char is
     * non-java char
     *
     * @param c
     *            - actual char
     * @param existNext
     *            - existing of next char behind actual char
     * @param partialLastChar
     *            - last char of partial converted identifier
     * @return converted char
     */
    private static String convert(final char c, final boolean existNext, final char partialLastChar) {
        return partialLastChar == '_' ? convertFirst(c, existNext) : "_" + convertFirst(c, existNext);
    }

    /**
     * Capitalize input string
     *
     * @param identifier
     *            - string to be capitalized
     */
    private static String capitalize(final String identifier) {
        return identifier.substring(FIRST_CHAR, FIRST_CHAR + 1).toUpperCase() + identifier.substring(1);
    }

}
