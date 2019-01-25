/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Collections2;
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
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DecimalTypeBuilder;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names) and to valid javadoc comments.
 */
public final class BindingGeneratorUtil {

    /**
     * Impossible to instantiate this class. All of the methods or attributes are static.
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
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    private static final Restrictions EMPTY_RESTRICTIONS = new Restrictions() {
        @Override
        public Optional<LengthConstraint> getLengthConstraint() {
            return Optional.empty();
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return Collections.emptyList();
        }

        @Override
        public Optional<RangeConstraint<?>> getRangeConstraint() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private static final Comparator<TypeMemberBuilder<?>> SUID_MEMBER_COMPARATOR =
        Comparator.comparing(TypeMemberBuilder::getName);

    private static final Comparator<Type> SUID_NAME_COMPARATOR = Comparator.comparing(Type::getFullyQualifiedName);

    /**
     * Converts <code>parameterName</code> to valid JAVA parameter name. If the <code>parameterName</code> is one
     * of the JAVA reserved words then it is prefixed with underscore character.
     *
     * @param parameterName string with the parameter name
     * @return string with the admissible parameter name
     */
    public static String resolveJavaReservedWordEquivalency(final String parameterName) {
        if (parameterName != null && BindingMapping.JAVA_RESERVED_WORDS.contains(parameterName)) {
            return "_" + parameterName;
        }
        return parameterName;
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package name for module)
     * and <code>schemaPath</code>. Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for which <code>schemaPath</code> is specified.
     *
     * @param basePackageName string with package name of the module, MUST be normalized, otherwise this method may
     *                        return an invalid string.
     * @param schemaPath list of names of YANG nodes which are parents of some node + name of this node
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
     * Creates package name from specified <code>basePackageName</code> (package name for module)
     * and <code>schemaPath</code> which crosses an augmentation. Resulting package name is concatenation
     * of <code>basePackageName</code> and all local names of YANG nodes which are parents of some node for which
     * <code>schemaPath</code> is specified.
     *
     * @param basePackageName string with package name of the module, MUST be normalized, otherwise this method may
     *                        return an invalid string.
     * @param schemaPath list of names of YANG nodes which are parents of some node + name of this node
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForAugmentedGeneratedType(final String basePackageName,
            final SchemaPath schemaPath) {
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
            final String nodeLocalName = iterator.next().getLocalName();
            // FIXME: Collon ":" is invalid in node local name as per RFC6020, identifier statement.
            builder.append(DASH_COLON_MATCHER.replaceFrom(nodeLocalName, '.'));
        }
        return BindingMapping.normalizePackageName(builder.toString());
    }

    private static <T> Iterable<T> sortedCollection(final Comparator<? super T> comparator, final Collection<T> input) {
        if (input.size() <= 1) {
            return input;
        }

        final List<T> ret = new ArrayList<>(input);
        ret.sort(comparator);
        return ret;
    }

    private static final ThreadLocal<MessageDigest> SHA1_MD = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to get a SHA digest provider", e);
        }
    });

    public static long computeDefaultSUID(final GeneratedTypeBuilderBase<?> to) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeUTF(to.getName());
            dout.writeInt(to.isAbstract() ? 3 : 7);

            for (final Type ifc : sortedCollection(SUID_NAME_COMPARATOR, filteredImplementsTypes(to))) {
                dout.writeUTF(ifc.getFullyQualifiedName());
            }

            for (final GeneratedPropertyBuilder gp : sortedCollection(SUID_MEMBER_COMPARATOR, to.getProperties())) {
                dout.writeUTF(gp.getName());
            }

            for (final MethodSignatureBuilder m : sortedCollection(SUID_MEMBER_COMPARATOR, to.getMethodDefinitions())) {
                if (!m.getAccessModifier().equals(AccessModifier.PRIVATE)) {
                    dout.writeUTF(m.getName());
                    dout.write(m.getAccessModifier().ordinal());
                }
            }

            dout.flush();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to hash object " + to, e);
        }

        final byte[] hashBytes = SHA1_MD.get().digest(bout.toByteArray());
        long hash = 0;
        for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
            hash = hash << 8 | hashBytes[i] & 0xFF;
        }
        return hash;
    }

    private static Collection<Type> filteredImplementsTypes(final GeneratedTypeBuilderBase<?> to) {
        return Collections2.filter(to.getImplementsTypes(), item -> !BindingTypes.TYPE_OBJECT.equals(item));
    }

    private static <T extends Optional<?>> T currentOrEmpty(final T current, final T base) {
        return current.equals(base) ? (T)Optional.empty() : current;
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
        for (final PatternConstraint c : constraints) {
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

                if (!tmp.getRangeConstraint().equals(decimal.getRangeConstraint())) {
                    return new Restrictions() {
                        @Override
                        public boolean isEmpty() {
                            return false;
                        }

                        @Override
                        public Optional<? extends RangeConstraint<?>> getRangeConstraint() {
                            return decimal.getRangeConstraint();
                        }

                        @Override
                        public List<PatternConstraint> getPatternConstraints() {
                            return ImmutableList.of();
                        }

                        @Override
                        public Optional<LengthConstraint> getLengthConstraint() {
                            return Optional.empty();
                        }
                    };
                }
            }

            return EMPTY_RESTRICTIONS;
        }

        final Optional<LengthConstraint> length;
        final List<PatternConstraint> pattern;
        final Optional<? extends RangeConstraint<?>> range;

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
                length = currentOrEmpty(binary.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = binary.getLengthConstraint();
            }

            pattern = ImmutableList.of();
            range = Optional.empty();
        } else if (type instanceof DecimalTypeDefinition) {
            length = Optional.empty();
            pattern = ImmutableList.of();

            final DecimalTypeDefinition decimal = (DecimalTypeDefinition)type;
            final DecimalTypeDefinition base = decimal.getBaseType();
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(decimal.getRangeConstraint(), base.getRangeConstraint());
            } else {
                range = decimal.getRangeConstraint();
            }
        } else if (type instanceof RangeRestrictedTypeDefinition) {
            // Integer-like types
            length = Optional.empty();
            pattern = ImmutableList.of();
            range = extractRangeConstraint((RangeRestrictedTypeDefinition<?, ?>)type);
        } else if (type instanceof StringTypeDefinition) {
            final StringTypeDefinition string = (StringTypeDefinition)type;
            final StringTypeDefinition base = string.getBaseType();
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(string.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = string.getLengthConstraint();
            }

            pattern = uniquePatterns(string);
            range = Optional.empty();
        } else {
            length = Optional.empty();
            pattern = ImmutableList.of();
            range = Optional.empty();
        }

        // Now, this may have ended up being empty, too...
        if (!length.isPresent() && pattern.isEmpty() && !range.isPresent()) {
            return EMPTY_RESTRICTIONS;
        }

        // Nope, not empty allocate a holder
        return new Restrictions() {
            @Override
            public Optional<? extends RangeConstraint<?>> getRangeConstraint() {
                return range;
            }

            @Override
            public List<PatternConstraint> getPatternConstraints() {
                return pattern;
            }

            @Override
            public Optional<LengthConstraint> getLengthConstraint() {
                return length;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    private static <T extends RangeRestrictedTypeDefinition<?, ?>> Optional<? extends RangeConstraint<?>>
            extractRangeConstraint(final T def) {
        final T base = (T) def.getBaseType();
        if (base != null && base.getBaseType() != null) {
            return currentOrEmpty(def.getRangeConstraint(), base.getRangeConstraint());
        }

        return def.getRangeConstraint();
    }

    /**
     * Encodes angle brackets in yang statement description.
     *
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

    public static String replaceAllIllegalChars(final CharSequence stringBuilder) {
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
