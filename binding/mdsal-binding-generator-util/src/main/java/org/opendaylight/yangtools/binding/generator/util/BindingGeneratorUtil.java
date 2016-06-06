/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DecimalTypeBuilder;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names) and to valid javadoc comments.
 *
 *
 */
public final class BindingGeneratorUtil {

    /**
     * Impossible to instantiate this class. All of the methods or attributes
     * are static.
     */
    private BindingGeneratorUtil() {
    }

    /**
     * Pre-compiled replacement pattern.
     */
    private static final CharMatcher DOT_MATCHER = CharMatcher.is('.');
    private static final CharMatcher DASH_COLON_MATCHER = CharMatcher.anyOf("-:");
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');

    private static final Restrictions EMPTY_RESTRICTIONS = new Restrictions() {
        @Override
        public List<LengthConstraint> getLengthConstraints() {
            return Collections.emptyList();
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return Collections.emptyList();
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private static final Comparator<TypeMemberBuilder<?>> SUID_MEMBER_COMPARATOR = new Comparator<TypeMemberBuilder<?>>() {
        @Override
        public int compare(final TypeMemberBuilder<?> o1, final TypeMemberBuilder<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static final Comparator<Type> SUID_NAME_COMPARATOR = new Comparator<Type>() {
        @Override
        public int compare(final Type o1, final Type o2) {
            return o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());
        }
    };

    /**
     * Converts <code>parameterName</code> to valid JAVA parameter name.
     *
     * If the <code>parameterName</code> is one of the JAVA reserved words then
     * it is prefixed with underscore character.
     *
     * @param parameterName
     *            string with the parameter name
     * @return string with the admissible parameter name
     */
    public static String resolveJavaReservedWordEquivalency(final String parameterName) {
        if (parameterName != null && BindingMapping.JAVA_RESERVED_WORDS.contains(parameterName)) {
            return "_" + parameterName;
        }
        return parameterName;
    }

    /**
     * Converts module name to valid JAVA package name.
     *
     * The package name consists of:
     * <ul>
     * <li>prefix - <i>org.opendaylight.yang.gen.v</i></li>
     * <li>module YANG version - <i>org.opendaylight.yang.gen.v</i></li>
     * <li>module namespace - invalid characters are replaced with dots</li>
     * <li>revision prefix - <i>.rev</i></li>
     * <li>revision - YYYYMMDD (MM and DD aren't spread to the whole length)</li>
     * </ul>
     *
     * @param module
     *            module which contains data about namespace and revision date
     * @return string with the valid JAVA package name
     * @throws IllegalArgumentException
     *             if the revision date of the <code>module</code> equals
     *             <code>null</code>
     * @deprecated USe {@link BindingMapping#getRootPackageName(QNameModule)} with {@link Module#getQNameModule()}.
     */
    @Deprecated
    public static String moduleNamespaceToPackageName(final Module module) {
        return BindingMapping.getRootPackageName(module.getQNameModule());
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
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
        final int size = Iterables.size(schemaPath.getPathTowardsRoot()) - 1;
        if (size <= 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, schemaPath.getPathFromRoot(), size);
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code> which crosses an augmentation.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
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
        final int size = Iterables.size(schemaPath.getPathTowardsRoot());
        if (size == 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, schemaPath.getPathFromRoot(), size);
    }

    private static String generateNormalizedPackageName(final String base, final Iterable<QName> path, final int size) {
        final StringBuilder builder = new StringBuilder(base);
        final Iterator<QName> iterator = path.iterator();
        for (int i = 0; i < size; ++i) {
            builder.append('.');
            String nodeLocalName = iterator.next().getLocalName();
            // FIXME: Collon ":" is invalid in node local name as per RFC6020, identifier statement.
            builder.append(DASH_COLON_MATCHER.replaceFrom(nodeLocalName, '.'));
        }
        return BindingMapping.normalizePackageName(builder.toString());
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * @param basePackageName
     *            string with package name of the module
     * @param schemaPath
     *            list of names of YANG nodes which are parents of some node +
     *            name of this node
     * @return string with valid JAVA package name
     *
     * @deprecated Use {@link #packageNameForGeneratedType(String, SchemaPath)} or
     *             {@link #packageNameForAugmentedGeneratedType(String, SchemaPath)} instead.
     */
    @Deprecated
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath,
            final boolean isUsesAugment) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (schemaPath == null) {
            throw new IllegalArgumentException("Schema Path cannot be NULL!");
        }

        final Iterable<QName> iterable = schemaPath.getPathFromRoot();
        final int size = Iterables.size(iterable);
        final int traversalSteps;
        if (isUsesAugment) {
            traversalSteps = size;
        } else {
            traversalSteps = size - 1;
        }

        if (traversalSteps == 0) {
            return BindingMapping.normalizePackageName(basePackageName);
        }

        return generateNormalizedPackageName(basePackageName, iterable, traversalSteps);
    }

    /**
     * Generates the package name for type definition from
     * <code>typeDefinition</code> and <code>basePackageName</code>.
     *
     * @param basePackageName
     *            string with the package name of the module
     * @param typeDefinition
     *            type definition for which the package name will be generated *
     * @return string with valid JAVA package name
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals <code>null</code></li>
     *             <li>if <code>typeDefinition</code> equals <code>null</code></li>
     *             </ul>
     * @deprecated This method ignores typeDefinition argument and its result is only
     *             <code>BindingMapping.normalizePackageName(basePackageName)</code>.
     *             Aside from tests, there is not a single user in OpenDaylight codebase,
     *             hence it can be considered buggy and defunct. It is scheduled for removal
     *             in Boron release.
     */
    @Deprecated
    public static String packageNameForTypeDefinition(final String basePackageName,
            final TypeDefinition<?> typeDefinition) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type Definition reference cannot be NULL!");
        }

        return BindingMapping.normalizePackageName(basePackageName);
    }

    /**
     * Converts <code>token</code> to string which is in accordance with best
     * practices for JAVA class names.
     *
     * @param token
     *            string which contains characters which should be converted to
     *            JAVA class name
     * @return string which is in accordance with best practices for JAVA class
     *         name.
     *
     * @deprecated Use {@link BindingMapping#getClassName(QName)} instead.
     */
    @Deprecated
    public static String parseToClassName(final String token) {
        return parseToCamelCase(token, true);
    }

    /**
     * Converts <code>token</code> to string which is in accordance with best
     * practices for JAVA parameter names.
     *
     * @param token
     *            string which contains characters which should be converted to
     *            JAVA parameter name
     * @return string which is in accordance with best practices for JAVA
     *         parameter name.
     *
     * @deprecated Use {@link BindingMapping#getPropertyName(String)} instead.
     */
    @Deprecated public static String parseToValidParamName(final String token) {
        return resolveJavaReservedWordEquivalency(parseToCamelCase(token, false));
    }

    /**
     *
     * Converts string <code>token</code> to the cammel case format.
     *
     * @param token
     *            string which should be converted to the cammel case format
     * @param uppercase
     *            boolean value which says whether the first character of the
     *            <code>token</code> should|shuldn't be uppercased
     * @return string in the cammel case format
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>token</code> without white spaces is empty</li>
     *             <li>if <code>token</code> equals null</li>
     *             </ul>
     */
    private static String parseToCamelCase(final String token, final boolean uppercase) {
        if (token == null) {
            throw new IllegalArgumentException("Name can not be null");
        }

        String correctStr = DOT_MATCHER.removeFrom(token.trim());
        if (correctStr.isEmpty()) {
            throw new IllegalArgumentException("Name can not be empty");
        }

        correctStr = replaceWithCamelCase(correctStr, ' ');
        correctStr = replaceWithCamelCase(correctStr, '-');
        correctStr = replaceWithCamelCase(correctStr, '_');

        char firstChar = correctStr.charAt(0);
        firstChar = uppercase ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar);

        if (firstChar >= '0' && firstChar <= '9') {
            return '_' + correctStr;
        } else {
            return firstChar + correctStr.substring(1);
        }
    }

    /**
     * Replaces all the occurrences of the <code>removalChar</code> in the
     * <code>text</code> with empty string and converts following character to
     * upper case.
     *
     * @param text
     *            string with source text which should be converted
     * @param removalChar
     *            character which is sought in the <code>text</code>
     * @return string which doesn't contain <code>removalChar</code> and has
     *         following characters converted to upper case
     * @throws IllegalArgumentException
     *             if the length of the returning string has length 0
     */
    private static String replaceWithCamelCase(final String text, final char removalChar) {
        int toBeRemovedPos = text.indexOf(removalChar);
        if (toBeRemovedPos == -1) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text);
        String toBeRemoved = String.valueOf(removalChar);
        do {
            sb.replace(toBeRemovedPos, toBeRemovedPos + 1, "");
            // check if 'toBeRemoved' character is not the only character in
            // 'text'
            if (sb.length() == 0) {
                throw new IllegalArgumentException("The resulting string can not be empty");
            }
            char replacement = Character.toUpperCase(sb.charAt(toBeRemovedPos));
            sb.setCharAt(toBeRemovedPos, replacement);
            toBeRemovedPos = sb.indexOf(toBeRemoved);
        } while (toBeRemovedPos != -1);

        return sb.toString();
    }

    private static <T> Iterable<T> sortedCollection(final Comparator<? super T> comparator, final Collection<T> input) {
        if (input.size() > 1) {
            final List<T> ret = new ArrayList<>(input);
            Collections.sort(ret, comparator);
            return ret;
        } else {
            return input;
        }
    }

    private static final ThreadLocal<MessageDigest> SHA1_MD = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to get a SHA digest provider", e);
            }
        }
    };

    public static long computeDefaultSUID(final GeneratedTypeBuilderBase<?> to) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeUTF(to.getName());
            dout.writeInt(to.isAbstract() ? 3 : 7);

            for (Type ifc : sortedCollection(SUID_NAME_COMPARATOR, to.getImplementsTypes())) {
                dout.writeUTF(ifc.getFullyQualifiedName());
            }

            for (GeneratedPropertyBuilder gp : sortedCollection(SUID_MEMBER_COMPARATOR, to.getProperties())) {
                dout.writeUTF(gp.getName());
            }

            for (MethodSignatureBuilder m : sortedCollection(SUID_MEMBER_COMPARATOR, to.getMethodDefinitions())) {
                if (!(m.getAccessModifier().equals(AccessModifier.PRIVATE))) {
                    dout.writeUTF(m.getName());
                    dout.write(m.getAccessModifier().ordinal());
                }
            }

            dout.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to hash object " + to, e);
        }

        final byte[] hashBytes = SHA1_MD.get().digest(bout.toByteArray());
        long hash = 0;
        for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
            hash = (hash << 8) | (hashBytes[i] & 0xFF);
        }
        return hash;
    }

    private static <T> List<T> currentOrEmpty(final List<T> current, final List<T> base) {
        return current.equals(base) ? ImmutableList.<T>of() : current;
    }

    private static boolean containsConstraint(final StringTypeDefinition type, final PatternConstraint constraint) {
        for (StringTypeDefinition wlk = type; wlk != null; wlk = wlk.getBaseType()) {
            if (wlk.getPatternConstraints().contains(constraint)) {
                return true;
            }
        }

        return false;
    }

    private static List<PatternConstraint> uniquePatterns(final StringTypeDefinition type) {
        final List<PatternConstraint> constraints = type.getPatternConstraints();
        if (constraints.isEmpty()) {
            return constraints;
        }

        final Builder<PatternConstraint> builder = ImmutableList.builder();
        boolean filtered = false;
        for (PatternConstraint c : constraints) {
            if (containsConstraint(type.getBaseType(), c)) {
                filtered = true;
            } else {
                builder.add(c);
            }
        }

        return filtered ? builder.build() : constraints;
    }

    public static Restrictions getRestrictions(final TypeDefinition<?> type) {
        // Old parser generated types which actually contained based restrictions, but our code deals with that when
        // binding to core Java types. Hence we'll emit empty restrictions for base types.
        if (type == null || type.getBaseType() == null) {
            // Handling of decimal64 has changed in the new parser. It contains range restrictions applied to the type
            // directly, without an extended type. We need to capture such constraints. In order to retain behavior we
            // need to analyze the new semantics and see if the constraints have been overridden. To do that we
            // instantiate a temporary unconstrained type and compare them.
            //
            // FIXME: looking at the generated code it looks as though we need to pass the restrictions without
            //        comparison
            if (type instanceof DecimalTypeDefinition) {
                final DecimalTypeDefinition decimal = (DecimalTypeDefinition) type;
                final DecimalTypeBuilder tmpBuilder = BaseTypes.decimalTypeBuilder(decimal.getPath());
                tmpBuilder.setFractionDigits(decimal.getFractionDigits());
                final DecimalTypeDefinition tmp = tmpBuilder.build();

                if (!tmp.getRangeConstraints().equals(decimal.getRangeConstraints())) {
                    return new Restrictions() {
                        @Override
                        public boolean isEmpty() {
                            return false;
                        }

                        @Override
                        public List<RangeConstraint> getRangeConstraints() {
                            return decimal.getRangeConstraints();
                        }

                        @Override
                        public List<PatternConstraint> getPatternConstraints() {
                            return ImmutableList.of();
                        }

                        @Override
                        public List<LengthConstraint> getLengthConstraints() {
                            return ImmutableList.of();
                        }
                    };
                }
            }

            return EMPTY_RESTRICTIONS;
        }

        final List<LengthConstraint> length;
        final List<PatternConstraint> pattern;
        final List<RangeConstraint> range;

        /*
         * Take care of extended types.
         *
         * Other types which support constraints are check afterwards. There is a slight twist with them, as returned
         * constraints are the effective view, e.g. they are inherited from base type. Since the constraint is already
         * enforced by the base type, we want to skip them and not perform duplicate checks.
         *
         * We end up emitting ConcreteType instances for YANG base types, which leads to their constraints not being
         * enforced (most notably decimal64). Therefore we need to make sure we do not strip the next-to-last
         * restrictions.
         *
         * FIXME: this probably not the best solution and needs further analysis.
         */
        if (type instanceof BinaryTypeDefinition) {
            final BinaryTypeDefinition binary = (BinaryTypeDefinition)type;
            final BinaryTypeDefinition base = binary.getBaseType();
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(binary.getLengthConstraints(), base.getLengthConstraints());
            } else {
                length = binary.getLengthConstraints();
            }

            pattern = ImmutableList.of();
            range = ImmutableList.of();
        } else if (type instanceof DecimalTypeDefinition) {
            length = ImmutableList.of();
            pattern = ImmutableList.of();

            final DecimalTypeDefinition decimal = (DecimalTypeDefinition)type;
            final DecimalTypeDefinition base = decimal.getBaseType();
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(decimal.getRangeConstraints(), base.getRangeConstraints());
            } else {
                range = decimal.getRangeConstraints();
            }
        } else if (type instanceof IntegerTypeDefinition) {
            length = ImmutableList.of();
            pattern = ImmutableList.of();

            final IntegerTypeDefinition integer = (IntegerTypeDefinition)type;
            final IntegerTypeDefinition base = integer.getBaseType();
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(integer.getRangeConstraints(), base.getRangeConstraints());
            } else {
                range = integer.getRangeConstraints();
            }
        } else if (type instanceof StringTypeDefinition) {
            final StringTypeDefinition string = (StringTypeDefinition)type;
            final StringTypeDefinition base = string.getBaseType();
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(string.getLengthConstraints(), base.getLengthConstraints());
            } else {
                length = string.getLengthConstraints();
            }

            pattern = uniquePatterns(string);
            range = ImmutableList.of();
        } else if (type instanceof UnsignedIntegerTypeDefinition) {
            length = ImmutableList.of();
            pattern = ImmutableList.of();

            final UnsignedIntegerTypeDefinition unsigned = (UnsignedIntegerTypeDefinition)type;
            final UnsignedIntegerTypeDefinition base = unsigned.getBaseType();
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(unsigned.getRangeConstraints(), base.getRangeConstraints());
            } else {
                range = unsigned.getRangeConstraints();
            }
        } else {
            length = ImmutableList.of();
            pattern = ImmutableList.of();
            range = ImmutableList.of();
        }

        // Now, this may have ended up being empty, too...
        if (length.isEmpty() && pattern.isEmpty() && range.isEmpty()) {
            return EMPTY_RESTRICTIONS;
        }

        // Nope, not empty allocate a holder
        return new Restrictions() {
            @Override
            public List<RangeConstraint> getRangeConstraints() {
                return range;
            }
            @Override
            public List<PatternConstraint> getPatternConstraints() {
                return pattern;
            }
            @Override
            public List<LengthConstraint> getLengthConstraints() {
                return length;
            }
            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    /**
     * Encodes angle brackets in yang statement description
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(String description) {
        if (description != null) {
            description = LT_MATCHER.replaceFrom(description, "&lt;");
            description = GT_MATCHER.replaceFrom(description, "&gt;");
        }
        return description;
    }
}
