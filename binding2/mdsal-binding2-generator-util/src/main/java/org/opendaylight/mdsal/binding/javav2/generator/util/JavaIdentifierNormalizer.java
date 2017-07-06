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
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;

/**
 * This util class converts every non-java char in identifier to java char by
 * its unicode name (<a href=
 * "http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8">JAVA SE
 * SPECIFICATIONS - Identifiers</a>). There are special types of mapping
 * non-java chars to original identifiers according to specific
 * {@linkplain JavaIdentifier java type}:
 * <ul>
 * <li>class, enum, interface</li>
 * <li>
 * <ul>
 * <li>without special separator</li>
 * <li>the first character of identifier, any other first character of
 * identifier part mapped by non-Java char name from unicode and char in
 * identifier behind non-java char name are converting to upper case</li>
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
 * <li>any other first character of identifier part mapped by non-Java char name
 * from unicode and char in identifier behind non-java char name are converting
 * to upper case</li>
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
 * <li>package - full package name (<a href=
 * "https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html">
 * Naming a package</a>)</li>
 * <li>
 * <li>
 * <ul>
 * <li>parts of package name are separated by dots</li>
 * <li>parts of package name are converting to lower case</li>
 * <li>if parts of package name are reserved Java or Windows keywords, such as
 * 'int' the suggested convention is to add an underscore to keyword</li>
 * <li>dash is parsed as underscore according to <a href=
 * "https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html">
 * Naming a package</a></li>
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
 * There is special case in CLASS, INTERFACE, ENUM, ENUM VALUE, CONSTANT, METHOD
 * and VARIABLE if identifier contains single dash - then the converter ignores
 * the single dash in the way of the non-java chars. In other way, if dash is
 * the first or the last char in the identifier or there is more dashes in a row
 * in the identifier, then these dashes are converted as non-java chars.
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
 * Next special case talks about normalizing class name which already exists in
 * package - but with different camel cases (foo, Foo, fOo, ...). To every next
 * classes with same names will by added their actual rank (serial number),
 * except the first one. This working for CLASS, ENUM and INTEFACE java
 * identifiers. If there exist the same ENUM VALUES in ENUM (with different
 * camel cases), then it's parsed with same logic like CLASSES, ENUMS and
 * INTERFACES but according to list of pairs of their ENUM parent. Example:
 *
 * <ul>
 * <li>class, enum, interface</li>
 * <li>
 * <ul>
 * <li>package name org.example, class (or interface or enum) Foo - normalized
 * to Foo
 * <li>package name org.example, class (or interface or enum) fOo - normalized
 * to Foo1
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
 * <li>YANG enum values will be mapped to 'FOO' and 'FOO_1' Java enum
 * values.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Beta
public final class JavaIdentifierNormalizer {

    public static final Set<String> SPECIAL_RESERVED_PATHS = ImmutableSet.of(
        "org.opendaylight.yangtools.concepts",
        "org.opendaylight.yangtools.yang.common",
        "org.opendaylight.yangtools.yang.model",
        "org.opendaylight.mdsal.binding.javav2.spec",
        "java",
        "com");

    private static final char UNDERSCORE = '_';
    private static final char DASH = '-';
    private static final String RESERVED_KEYWORD = "reserved_keyword";
    private static final Set<String> PRIMITIVE_TYPES = ImmutableSet.of("char[]", "byte[]");

    private static final CharMatcher DASH_MATCHER = CharMatcher.is(DASH);
    private static final CharMatcher DASH_OR_SPACE_MATCHER = CharMatcher.anyOf(" -");
    private static final CharMatcher UNDERSCORE_MATCHER = CharMatcher.is(UNDERSCORE);
    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Splitter UNDERSCORE_SPLITTER = Splitter.on(UNDERSCORE);

    // Converted to lower case
    private static final Set<String> WINDOWS_RESERVED_WORDS = BindingMapping.WINDOWS_RESERVED_WORDS.stream()
            .map(String::toLowerCase).collect(ImmutableSet.toImmutableSet());

    private JavaIdentifierNormalizer() {
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
    public static String normalizeEnumValueIdentifier(final String name, final List<Pair> values) {
        return convertIdentifierEnumValue(name, name, values, 1);
    }

    /**
     * Normalizing full package name by non java chars and reserved keywords.
     *
     * @param fullPackageName
     *            - full package name
     * @return normalized name
     */
    public static String normalizeFullPackageName(final String fullPackageName) {
        final Iterator<String> it = DOT_SPLITTER.split(fullPackageName).iterator();
        if (!it.hasNext()) {
            return fullPackageName;
        }

        final StringBuilder sb = new StringBuilder(fullPackageName.length());
        while (true) {
            sb.append(normalizePartialPackageName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append('.');
        }
    }

    /**
     * Normalizing part of package name by non java chars.
     *
     * @param packageNamePart
     *            - part of package name
     * @return normalized name
     */
    public static String normalizePartialPackageName(final String packageNamePart) {
        // if part of package name consist from java or windows reserved word, return it with
        // underscore at the end and in lower case
        final String lowerPart = packageNamePart.toLowerCase();
        if (BindingMapping.JAVA_RESERVED_WORDS.contains(lowerPart) || WINDOWS_RESERVED_WORDS.contains(lowerPart)) {
            return lowerPart + UNDERSCORE;
        }

        final String normalizedPart = DASH_MATCHER.replaceFrom(packageNamePart, UNDERSCORE);

        final StringBuilder sb = new StringBuilder();
        final StringBuilder innerSb = new StringBuilder();
        for (int i = 0; i < normalizedPart.length(); i++) {
            final char c = normalizedPart.charAt(i);
            if (c == UNDERSCORE) {
                if (innerSb.length() != 0) {
                    sb.append(normalizeSpecificIdentifier(innerSb.toString(), JavaIdentifier.PACKAGE));
                    innerSb.setLength(0);
                }
                sb.append(UNDERSCORE);
            } else {
                innerSb.append(c);
            }
        }
        if (innerSb.length() != 0) {
            sb.append(normalizeSpecificIdentifier(innerSb.toString(), JavaIdentifier.PACKAGE));
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
    public static String normalizeClassIdentifier(final String packageName, final String className, ModuleContext
            context) {
        if (packageName.isEmpty() && PRIMITIVE_TYPES.contains(className)) {
            return className;
        }
        for (final String reservedPath : SPECIAL_RESERVED_PATHS) {
            if (packageName.startsWith(reservedPath)) {
                return className;
            }
        }
        final String convertedClassName = normalizeSpecificIdentifier(className, JavaIdentifier.CLASS);

        // if packageName contains class name at the end, then the className is name of inner class
        final String basePackageName;
        final int lastDot = packageName.lastIndexOf('.');
        if (lastDot != -1 && Character.isUpperCase(packageName.charAt(lastDot + 1))) {
            // ignore class name in package name - inner class name has to be normalized according to original package
            // of parent class
            basePackageName = packageName.substring(0, lastDot);
        } else {
            basePackageName = packageName;
        }

        return normalizeClassIdentifier(basePackageName, convertedClassName, convertedClassName, 1, context);
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
    public static String normalizeSpecificIdentifier(final String identifier, final JavaIdentifier javaIdentifier) {
        // if identifier isn't PACKAGE type then check it by reserved keywords
        if (javaIdentifier != JavaIdentifier.PACKAGE) {
            final String lower = identifier.toLowerCase();
            if (BindingMapping.JAVA_RESERVED_WORDS.contains(lower) || WINDOWS_RESERVED_WORDS.contains(lower)) {
                return fixCasesByJavaType(lower + UNDERSCORE + RESERVED_KEYWORD, javaIdentifier);
            }
        }

        // check and convert first char in identifier if there is non-java char
        final StringBuilder sb = new StringBuilder();
        final char firstChar = identifier.charAt(0);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            // converting first char of identifier
            sb.append(convertFirst(firstChar, existNext(identifier, 0)));
        } else {
            sb.append(firstChar);
        }
        // check and convert other chars in identifier, if there is non-java char
        for (int i = 1; i < identifier.length(); i++) {
            final char actualChar = identifier.charAt(i);
            // ignore single dash as non java char - if there is more dashes in a row or dash is as
            // the last char in identifier then parse these dashes as non java chars
            if (actualChar == DASH && existNext(identifier, i)) {
                if (identifier.charAt(i - 1) != DASH && identifier.charAt(i + 1) != DASH) {
                    sb.append(UNDERSCORE);
                    continue;
                }
            }
            if (!Character.isJavaIdentifierPart(actualChar)) {
                // prepare actual string of sb for checking if underscore exist on position of the last char
                sb.append(convert(actualChar, existNext(identifier, i), sb.charAt(sb.length() - 1)));
            } else {
                sb.append(actualChar);
            }
        }
        // apply camel case in appropriate way
        return fixCasesByJavaType(sb.toString().replace("__", "_").toLowerCase(), javaIdentifier);
    }

    /**
     * Checking while there doesn't exist any class name with the same name
     * (regardless of camel cases) in package.
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
            final String actualClassName, final int rank, ModuleContext context) {

        final ListMultimap<String, String> packagesMap = context.getPackagesMap();

        synchronized (packagesMap) {
            if (packagesMap.containsKey(packageName)) {
                for (final String existingName : packagesMap.get(packageName)) {
                    if (actualClassName.equalsIgnoreCase(existingName)) {
                       return normalizeClassIdentifier(packageName, origClassName, origClassName + rank,
                     rank + 1, context);
                    }
                }
            }
            context.putToPackagesMap(packageName, actualClassName);
            return actualClassName;
        }
    }

    /**
     * Fix cases of converted identifiers by Java type
     *
     * @param convertedIdentifier
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
                return UNDERSCORE_MATCHER.removeFrom(convertedIdentifier);
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
        if (convertedIdentifier.indexOf(UNDERSCORE) == -1) {
            return convertedIdentifier;
        }

        final StringBuilder sb = new StringBuilder(convertedIdentifier.length());
        final Iterator<String> it = UNDERSCORE_SPLITTER.split(convertedIdentifier).iterator();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(capitalize(it.next()));
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
        return identifier.length() > actual + 1;
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
        final String name = DASH_OR_SPACE_MATCHER.replaceFrom(Character.getName(c), UNDERSCORE);
        return existNext ? name + '_' : name;
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
        return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
    }

    private static String convertIdentifierEnumValue(final String name, final String origName, final List<Pair> values,
            final int rank) {
        String newName = name;
        for (final Pair pair : values) {
            if (name.equalsIgnoreCase(pair.getName()) || name.equalsIgnoreCase(pair.getMappedName())) {
                int actualRank = rank;
                final String actualName = origName + UNDERSCORE + actualRank;
                newName = convertIdentifierEnumValue(actualName, origName, values, ++actualRank);
            }
        }
        return normalizeSpecificIdentifier(newName, JavaIdentifier.ENUM_VALUE);
    }
}
