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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Iterables;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
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
 * Standard Util class that contains various method for converting
 * input strings to valid JAVA language strings e.g. package names,
 * class names, attribute names and/or valid JavaDoc comments.
 */
@Beta
public final class BindingGeneratorUtil {

    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');

    private static final Interner<String> PACKAGE_INTERNER = Interners.newWeakInterner();
    private static final Comparator<TypeMemberBuilder<?>> SUID_MEMBER_COMPARATOR =
            Comparator.comparing(TypeMemberBuilder::getName);

    private static final Comparator<Type> SUID_NAME_COMPARATOR =
            Comparator.comparing(Type::getFullyQualifiedName);

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

    private BindingGeneratorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Encodes angle brackets in yang statement description
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(final String description) {
        String newDesc = description;
        if (newDesc != null) {
            newDesc = LT_MATCHER.replaceFrom(newDesc, "&lt;");
            newDesc = GT_MATCHER.replaceFrom(newDesc, "&gt;");
        }
        return newDesc;
    }

    public static long computeDefaultSUID(final GeneratedTypeBuilderBase<?> to) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeUTF(to.getName());
            dout.writeInt(to.isAbstract() ? 3 : 7);

            for (final Type ifc : sortedCollection(SUID_NAME_COMPARATOR, to.getImplementsTypes())) {
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

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
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
    public static String packageNameForGeneratedType(final String basePackageName, final SchemaPath schemaPath, final
        BindingNamespaceType namespaceType) {

        final Iterable<QName> pathTowardsRoot = schemaPath.getPathTowardsRoot();
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        final int size = Iterables.size(pathTowardsRoot) - 1;
        if (size <= 0) {
            if (namespaceType != null) {
                final StringBuilder sb = new StringBuilder();
                sb.append(basePackageName)
                  .append('.')
                  .append(namespaceType.getPackagePrefix());
                return JavaIdentifierNormalizer.normalizeFullPackageName(sb.toString());
            }
            return JavaIdentifierNormalizer.normalizeFullPackageName(basePackageName);
        }

        return generateNormalizedPackageName(basePackageName, pathFromRoot, size, namespaceType);
    }

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>namespaceType</code>.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and prefix of <code>namespaceType</code>.
     *
     * @param basePackageName
     *            string with package name of the module, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param namespaceType
     *            the namespace to which the module belongs
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameWithNamespacePrefix(final String basePackageName,
            final BindingNamespaceType namespaceType) {
        final StringBuilder sb = new StringBuilder();
        sb.append(basePackageName)
                .append('.')
                .append(namespaceType.getPackagePrefix());
        return JavaIdentifierNormalizer.normalizeFullPackageName(sb.toString());
    }

    public static Restrictions getRestrictions(final TypeDefinition<?> type) {
        if (type == null || type.getBaseType() == null) {
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
                        public Optional<RangeConstraint<BigDecimal>> getRangeConstraint() {
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

    /**
     * Creates package name from specified <code>basePackageName</code> (package
     * name for module) and <code>schemaPath</code> which crosses an augmentation.
     *
     * Resulting package name is concatenation of <code>basePackageName</code>
     * and all local names of YANG nodes which are parents of some node for
     * which <code>schemaPath</code> is specified.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
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
        final Iterable<QName> pathTowardsRoot = schemaPath.getPathTowardsRoot();
        final Iterable<QName> pathFromRoot = schemaPath.getPathFromRoot();
        final int size = Iterables.size(pathTowardsRoot);
        if (size == 0) {
            return basePackageName;
        }

        return generateNormalizedPackageName(basePackageName, pathFromRoot, size, BindingNamespaceType.Data);
    }

    /**
     * Creates package name from <code>parentAugmentPackageName</code> (package
     * name for direct parent augmentation) and <code>augmentationSchema</code> .
     *
     * Resulting package name is concatenation of <code>parentAugmentPackageName</code>
     * and the local name of <code>schemaPath</code>.
     *
     * Based on type of node, there is also possible suffix added in order
     * to prevent package name conflicts.
     *
     * @param parentAugmentPackageName
     *            string with package name of direct parent augmentation, MUST be normalized,
     *            otherwise this method may return an invalid string.
     * @param augmentationSchema
     *            augmentation schema which is direct son of parent augmentation.
     * @return string with valid JAVA package name
     * @throws NullPointerException if any of the arguments are null
     */
    public static String packageNameForAugmentedGeneratedType(final String parentAugmentPackageName,
                                                              final AugmentationSchemaNode augmentationSchema) {
        final QName last = augmentationSchema.getTargetPath().getLastComponent();

        return generateNormalizedPackageName(parentAugmentPackageName, last);
    }

    public static String packageNameForSubGeneratedType(final String basePackageName, final SchemaNode node,
                                                        final BindingNamespaceType namespaceType) {
        final String parent = packageNameForGeneratedType(basePackageName, node.getPath(), namespaceType);
        final QName last = node.getPath().getLastComponent();

        return generateNormalizedPackageName(parent, last);
    }

    public static String replacePackageTopNamespace(final String basePackageName,
            final String toReplacePackageName,
            final BindingNamespaceType toReplaceNameSpace,
            final BindingNamespaceType replacedNameSpace) {
        Preconditions.checkArgument(basePackageName != null);
        String normalizeBasePackageName = JavaIdentifierNormalizer.normalizeFullPackageName(basePackageName);

        if (!normalizeBasePackageName.equals(toReplacePackageName)) {
            final String topPackageName = new StringBuilder(normalizeBasePackageName)
                    .append('.').append(toReplaceNameSpace.getPackagePrefix()).toString();

            Preconditions.checkState(toReplacePackageName.equals(topPackageName)
                            || toReplacePackageName.contains(topPackageName),
                    "Package name to replace does not belong to the given namespace to replace!");

            return new StringBuilder(normalizeBasePackageName)
                    .append('.')
                    .append(replacedNameSpace.getPackagePrefix())
                    .append(toReplacePackageName.substring(topPackageName.length()))
                    .toString();
        } else {
            return new StringBuilder(normalizeBasePackageName)
                    .append('.').append(replacedNameSpace.getPackagePrefix()).toString();
        }
    }

    private static final ThreadLocal<MessageDigest> SHA1_MD = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA");
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to get a SHA digest provider", e);
            }
        }
    };

    private static String generateNormalizedPackageName(final String base, final Iterable<QName> path, final int
            size, final BindingNamespaceType namespaceType) {
        final StringBuilder builder = new StringBuilder(base);
        if (namespaceType != null) {
            builder.append('.').append(namespaceType.getPackagePrefix());
        }
        final Iterator<QName> iterator = path.iterator();
        for (int i = 0; i < size; ++i) {
            builder.append('.');
            final String nodeLocalName = iterator.next().getLocalName();
            builder.append(nodeLocalName);
        }
        final String normalizedPackageName = JavaIdentifierNormalizer.normalizeFullPackageName(builder.toString());
        // Prevent duplication of input
        PACKAGE_INTERNER.intern(normalizedPackageName);
        return normalizedPackageName;
    }

    private static String generateNormalizedPackageName(final String parent, final QName path) {
        final StringBuilder sb = new StringBuilder(parent)
                .append('.')
                .append(path.getLocalName());

        final String normalizedPackageName = JavaIdentifierNormalizer.normalizeFullPackageName(sb.toString());
        // Prevent duplication of input
        PACKAGE_INTERNER.intern(normalizedPackageName);
        return normalizedPackageName;
    }

    private static <T> Iterable<T> sortedCollection(final Comparator<? super T> comparator, final Collection<T> input) {
        if (input.size() > 1) {
            final List<T> ret = new ArrayList<>(input);
            ret.sort(comparator);
            return ret;
        } else {
            return input;
        }
    }

    private static <T extends Optional<?>> T currentOrEmpty(final T current, final T base) {
        return current.equals(base) ? (T)Optional.empty() : current;
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

    private static boolean containsConstraint(final StringTypeDefinition type, final PatternConstraint constraint) {
        for (StringTypeDefinition wlk = type; wlk != null; wlk = wlk.getBaseType()) {
            if (wlk.getPatternConstraints().contains(constraint)) {
                return true;
            }
        }

        return false;
    }

    private static <T extends RangeRestrictedTypeDefinition<?, ?>> Optional<? extends RangeConstraint<?>>
            extractRangeConstraint(final T def) {
        final T base = (T) def.getBaseType();
        if (base != null && base.getBaseType() != null) {
            return currentOrEmpty(def.getRangeConstraint(), base.getRangeConstraint());
        }

        return def.getRangeConstraint();
    }

}
