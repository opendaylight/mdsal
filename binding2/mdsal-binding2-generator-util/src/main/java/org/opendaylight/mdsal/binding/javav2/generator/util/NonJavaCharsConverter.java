/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

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
 * <li>int - IntReservedKeyword</li>
 * <li>con - ConReservedKeyword</li>
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
 * <li>int - INT_RESERVED_KEYWORD</li>
 * <li>con - CON_RESERVED_KEYWORD</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>method, variable</li>
 * <li>
 * <li>
 * <ul>
 * <li>without special separator</li>
 * <li>the first character of identifier is converting to lower case</li>
 * <li>any other first character of identifier part mapped by non-Java char name from unicode and
 * char in identifier behind non-java char name are converting to upper case</li>
 * <li>examples:</li>
 * <li>
 * <ul>
 * <li>example* - exampleAsterisk</li>
 * <li>example*example - exampleAserisksExample</li>
 * <li>\example - reverseSolidusExample</li>
 * <li>1example - digitOneExample</li>
 * <li>example1 - example1</li>
 * <li>int - intReservedKeyword</li>
 * <li>con - conReservedKeyword</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>package - full package name
 * (<a href="https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html"> Naming a
 * package</a>)</li>
 * <li>
 * <li>
 * <ul>
 * <li>parts of package name are separated by dots</li>
 * <li>parts of package name are converting to lower case</li>
 * <li>if parts of package name are reserved Java or Windows keywords, such as 'int' the suggested
 * convention is to add an underscore to keyword</li>
 * <li>dash is parsed as underscore according to
 * <a href="https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html"> Naming a
 * package</a></li>
 * <li>examples:</li>
 * <li>
 * <ul>
 * <li>org.example* - org.exampleasterisk</li>
 * <li>org.example*example - org.exampleasteriskexample</li>
 * <li>org.\example - org.reversesolidusexample</li>
 * <li>org.1example - org.digitoneexample</li>
 * <li>org.example1 - org.example1</li>
 * <li>org.int - org.int_</li>
 * <li>org.con - org.con_</li>
 * <li>org.foo-cont - org.foo_cont</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 * There is special case in CLASS, INTERFACE, ENUM, ENUM VALUE, CONSTANT, METHOD and VARIABLE if
 * identifier contains single dash - then the converter ignores the single dash in the way of the
 * non-java chars. In other way, if dash is the first or the last char in the identifier or there is
 * more dashes in a row in the identifier, then these dashes are converted as non-java chars.
 * Example:
 * <ul>
 * <li>class, enum, interface</li>
 * <li>
 * <ul>
 * <li>foo-cont - FooCont</li>
 * <li>foo--cont - FooHyphenMinusHyphenMinusCont</li>
 * <li>-foo - HyphenMinusFoo</li>
 * <li>foo- - FooHyphenMinus</li>
 * </ul>
 * </li>
 * <li>enum value, constant
 * <li>
 * <ul>
 * <li>foo-cont - FOO_CONT</li>
 * <li>foo--cont - FOO_HYPHEN_MINUS_HYPHEN_MINUS_CONT</li>
 * <li>-foo - HYPHEN_MINUS_FOO</li>
 * <li>foo- - FOO_HYPHEN_MINUS</li>
 * </ul>
 * </li>
 * <li>method, variable</li>
 * <li>
 * <ul>
 * <li>foo-cont - fooCont</li>
 * <li>foo--cont - fooHyphenMinusHyphenMinusCont</li>
 * <li>-foo - hyphenMinusFoo</li>
 * <li>foo- - fooHyphenMinus</li>
 * </ul>
 * </li>
 * </ul>
 *
 * Next special case talks about normalizing class name which already exists in package - but with
 * different camel cases (foo, Foo, fOo, ...). To every next classes with same names will by added
 * their actual rank (serial number), except the first one. This working for CLASS, ENUM and
 * INTEFACE java identifiers. If there exist the same ENUM VALUES in ENUM (with different camel
 * cases), then it's parsed with same logic like CLASSES, ENUMS and INTERFACES but according to list
 * of pairs of their ENUM parent. Example:
 *
 * <ul>
 * <li>class, enum, interface</li>
 * <li>
 * <ul>
 * <li>package name org.example, class (or interface or enum) Foo - normalized to Foo
 * <li>package name org.example, class (or interface or enum) fOo - normalized to Foo1
 * </ul>
 * </li>
 * <li>enum value</li>
 * <li>
 * <ul>
 * <li>
 *
 * <pre>
 * type enumeration {
 *     enum foo;
 *     enum Foo;
 * }
 * </pre>
 *
 * </li>
 * <li>YANG enum values will be mapped to 'FOO' and 'FOO_1' Java enum values.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Beta
public final class NonJavaCharsConverter {

    private static final int FIRST_CHAR = 0;
    private static final int FIRST_INDEX = 1;
    private static final char UNDERSCORE = '_';
    private static final char DASH = '-';
    private static final String EMPTY_STRING = "";
    private static final String RESERVED_KEYWORD = "reserved_keyword";
    private static final ListMultimap<String, String> PACKAGES_MAP = ArrayListMultimap.create();

    private NonJavaCharsConverter() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * <p>
     * According to <a href="https://tools.ietf.org/html/rfc7950#section-9.6.4">YANG RFC 7950</a>,
     * all assigned names in an enumeration MUST be unique. Created names are contained in the list
     * of {@link Enumeration.Pair}. This method adds actual index with underscore behind name of new
     * enum value only if this name already exists in one of the list of {@link Enumeration.Pair}.
     * Then, the name will be converted to java chars according to {@link JavaIdentifier#ENUM_VALUE}
     * and returned.
     * </p>
     * Example:
     *
     * <pre>
     * type enumeration {
     *     enum foo;
     *     enum Foo;
     * }
     * </pre>
     *
     * YANG enum values will be mapped to 'FOO' and 'FOO_1' Java enum values.
     *
     * @param name
     *            - name of new enum value
     * @param values
     *            - list of all actual enum values
     * @return converted and fixed name of new enum value
     */
    public static String convertIdentifierEnumValue(final String name, final List<Pair> values) {
        return convertIdentifierEnumValue(name, name, values, FIRST_INDEX);
    }

    /**
     * Normalizing full package name by non java chars and reserved keywords.
     *
     * @param fullPackageName
     *            - full package name
     * @return normalized name
     */
    public static String convertFullPackageName(final String fullPackageName) {
        final String[] packageNameParts = fullPackageName.split("\\.");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < packageNameParts.length; i++) {
            sb.append(NonJavaCharsConverter.normalizePackageNamePart(packageNameParts[i]));
            if (i != (packageNameParts.length - 1)) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    /**
     * Normalizing part of package name by non java chars.
     *
     * @param packageNamePart
     *            - part of package name
     * @return normalized name
     */
    @VisibleForTesting
    public static String normalizePackageNamePart(final String packageNamePart) {
        // if part of package name consist from java or windows reserved word, return it with
        // underscore at the end and in lower case
        if (BindingMapping.JAVA_RESERVED_WORDS.contains(packageNamePart.toLowerCase())
                || BindingMapping.WINDOWS_RESERVED_WORDS.contains(packageNamePart.toUpperCase())) {
            return new StringBuilder(packageNamePart).append(UNDERSCORE).toString().toLowerCase();
        }
        String normalizedPackageNamePart = packageNamePart;
        if (packageNamePart.contains(String.valueOf(DASH))) {
            normalizedPackageNamePart = packageNamePart.replaceAll(String.valueOf(DASH), String.valueOf(UNDERSCORE));
        }
        final StringBuilder sb = new StringBuilder();
        StringBuilder innserSb = new StringBuilder();
        for (int i = 0; i < normalizedPackageNamePart.length(); i++) {
            if (normalizedPackageNamePart.charAt(i) == UNDERSCORE) {
                if (!innserSb.toString().isEmpty()) {
                    sb.append(convertIdentifier(innserSb.toString(), JavaIdentifier.PACKAGE));
                    innserSb = new StringBuilder();
                }
                sb.append(UNDERSCORE);
            } else {
                innserSb.append(normalizedPackageNamePart.charAt(i));
            }
        }
        if (!innserSb.toString().isEmpty()) {
            sb.append(convertIdentifier(innserSb.toString(), JavaIdentifier.PACKAGE));
        }
        // returned normalized part of package name
        return sb.toString();
    }

    /**
     * Find and convert non Java chars in identifiers of generated transfer objects, initially
     * derived from corresponding YANG according to
     * <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8"> Java
     * Specifications - Identifiers</a>. If there is more same class names at the same package, then
     * append rank (serial number) to the end of them. Works for class, enum, interface.
     *
     * @param packageName
     *            - package of identifier
     * @param className
     *            - name of identifier
     * @return - java acceptable identifier
     */
    public static String normalizeClassIdentifier(final String packageName, final String className) {
        final String convertedClassName = convertIdentifier(className, JavaIdentifier.CLASS);
        return normalizeClassIdentifier(packageName, convertedClassName, convertedClassName, FIRST_INDEX);
    }

    /**
     * Checking while there doesn't exist any class name with the same name (regardless of camel
     * cases) in package.
     *
     * @param packageName
     *            - package of class name
     * @param origClassName
     *            - original class name
     * @param actualClassName
     *            - actual class name with rank (serial number)
     * @param rank
     *            - actual rank (serial number)
     * @return converted identifier
     */
    private static String normalizeClassIdentifier(final String packageName, final String origClassName,
            final String actualClassName, final int rank) {
        if (PACKAGES_MAP.containsKey(packageName)) {
            for (final String existingName : PACKAGES_MAP.get(packageName)) {
                if (existingName.toLowerCase().equals(actualClassName.toLowerCase())) {
                    final int nextRank = rank + 1;
                    return normalizeClassIdentifier(packageName, origClassName,
                            new StringBuilder(origClassName).append(rank).toString(), nextRank);
                }
            }
        }
        PACKAGES_MAP.put(packageName, actualClassName);
        return actualClassName;
    }

    /**
     * Find and convert non Java chars in identifiers of generated transfer objects, initially
     * derived from corresponding YANG.
     *
     * @param identifier
     *            - name of identifier
     * @param javaIdentifier
     *            - java type of identifier
     * @return - java acceptable identifier
     */
    public static String convertIdentifier(final String identifier, final JavaIdentifier javaIdentifier) {
        final StringBuilder sb = new StringBuilder();

        // if identifier isn't PACKAGE type then check it by reserved keywords
        if(javaIdentifier != JavaIdentifier.PACKAGE) {
            if (BindingMapping.JAVA_RESERVED_WORDS.contains(identifier.toLowerCase())
                    || BindingMapping.WINDOWS_RESERVED_WORDS.contains(identifier.toUpperCase())) {
                return fixCasesByJavaType(
                        sb.append(identifier).append(UNDERSCORE).append(RESERVED_KEYWORD).toString().toLowerCase(),
                        javaIdentifier);
            }
        }

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
            // ignore single dash as non java char - if there is more dashes in a row or dash is as
            // the last char in identifier then parse these dashes as non java chars
            if ((actualChar == '-') && existNext(identifier, i)) {
                if ((identifier.charAt(i - 1) != DASH) && (identifier.charAt(i + 1) != DASH)) {
                    sb.append(UNDERSCORE);
                    continue;
                }
            }
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
        return fixCasesByJavaType(sb.toString().replace("__", "_").toLowerCase(), javaIdentifier);
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
            case PACKAGE:
                return convertedIdentifier.replaceAll(String.valueOf(UNDERSCORE), EMPTY_STRING);
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
        if (convertedIdentifier.contains(String.valueOf(UNDERSCORE))) {
            boolean isFirst = true;
            for (final String part : convertedIdentifier.split(String.valueOf(UNDERSCORE))) {
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
        if (name.contains(String.valueOf(DASH))) {
            name = name.replaceAll(String.valueOf(DASH), String.valueOf(UNDERSCORE));
        }
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

    private static String convertIdentifierEnumValue(final String name, final String origName, final List<Pair> values,
            final int rank) {
        String newName = name;
        for (final Pair pair : values) {
            if (pair.getName().toLowerCase().equals(name.toLowerCase())
                    || pair.getMappedName().toLowerCase().equals(name.toLowerCase())) {
                int actualRank = rank;
                final StringBuilder actualNameBuilder =
                        new StringBuilder(origName).append(UNDERSCORE).append(actualRank);
                newName = convertIdentifierEnumValue(actualNameBuilder.toString(), origName, values, ++actualRank);
            }
        }
        return convertIdentifier(newName, JavaIdentifier.ENUM_VALUE);
    }
}
