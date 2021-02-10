/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

/**
 * Compatibility utilities for dealing with differences between the old parser's ExtendedType-driven type
 * representation versus the representation provided by {@code yang-model-util} models.
 */
public final class CompatUtils {
    private CompatUtils() {
        // Hidden on purpose
    }

    /**
     * This package's type hierarchy model generates a type which encapsulates the default value and units for leaves.
     * Java Binding specification is implemented in a way, where it needs to revert this process if the internal
     * declaration has not restricted the type further -- which is not something available via
     * {@link TypeDefinition#getBaseType()}.
     *
     * <p>
     * Here are the possible scenarios:
     *
     * <pre>
     * leaf foo {
     *     type uint8 {
     *         range 1..2;
     *     }
     * }
     * </pre>
     * The leaf type's schema path does not match the schema path of the leaf. We do NOT want to strip it, as
     * we need to generate an inner class to hold the restrictions.
     *
     * <pre>
     * leaf foo {
     *     type uint8 {
     *         range 1..2;
     *     }
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf. We do NOT want to strip it, as we need
     * to generate an inner class to hold the restrictions.
     *
     * <pre>
     * leaf foo {
     *     type uint8;
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf. We DO want to strip it, as we will deal
     * with the default value ourselves.
     *
     * <pre>
     * leaf foo {
     *     type uint8;
     * }
     * </pre>
     * The leaf type's schema path will not match the schema path of the leaf. We do NOT want to strip it.
     *
     * <p>
     * The situation is different for types which do not have a default instantiation in YANG: leafref, enumeration,
     * identityref, decimal64, bits and union. If these types are defined within this leaf's statement, a base type
     * will be instantiated. If the leaf defines a default statement, this base type will be visible via getBaseType().
     *
     * <pre>
     * leaf foo {
     *     type decimal64 {
     *         fraction-digits 2;
     *     }
     * }
     * </pre>
     * The leaf type's schema path will not match the schema path of the leaf, and we do not want to strip it, as it
     * needs to be generated.
     *
     * <pre>
     * leaf foo {
     *     type decimal64 {
     *         fraction-digits 2;
     *     }
     *     default 1;
     * }
     * </pre>
     * The leaf type's schema path will match the schema path of the leaf, and we DO want to strip it.
     *
     * @param leaf Leaf for which we are acquiring the type
     * @return Potentially base type of the leaf type.
     */
    public static @NonNull TypeDefinition<?> compatType(final @NonNull TypedDataSchemaNode leaf) {
        final TypeDefinition<?> leafType = requireNonNull(leaf.getType());

        if (!leaf.getPath().equals(leafType.getPath())) {
            // Old parser semantics, or no new default/units defined for this leaf
            return leafType;
        }

        // We are dealing with a type generated for the leaf itself
        final TypeDefinition<?> baseType = leafType.getBaseType();
        checkArgument(baseType != null, "Leaf %s has type for leaf, but no base type", leaf);

        if (leaf.getPath().equals(baseType.getPath().getParent())) {
            // Internal instantiation of a base YANG type (decimal64 and similar)
            return baseType;
        }

        // At this point we have dealt with the easy cases. Now we need to perform per-type checking if there are no
        // new constraints introduced by this type. If there were not, we will return the base type.
        if (leafType instanceof BinaryTypeDefinition) {
            return baseTypeIfNotConstrained((BinaryTypeDefinition) leafType);
        } else if (leafType instanceof DecimalTypeDefinition) {
            return baseTypeIfNotConstrained((DecimalTypeDefinition) leafType);
        } else if (leafType instanceof InstanceIdentifierTypeDefinition) {
            return baseTypeIfNotConstrained((InstanceIdentifierTypeDefinition) leafType);
        } else if (leafType instanceof Int8TypeDefinition) {
            return baseTypeIfNotConstrained((Int8TypeDefinition) leafType);
        } else if (leafType instanceof Int16TypeDefinition) {
            return baseTypeIfNotConstrained((Int16TypeDefinition) leafType);
        } else if (leafType instanceof Int32TypeDefinition) {
            return baseTypeIfNotConstrained((Int32TypeDefinition) leafType);
        } else if (leafType instanceof Int64TypeDefinition) {
            return baseTypeIfNotConstrained((Int64TypeDefinition) leafType);
        } else if (leafType instanceof StringTypeDefinition) {
            return baseTypeIfNotConstrained((StringTypeDefinition) leafType);
        } else if (leafType instanceof Uint8TypeDefinition) {
            return baseTypeIfNotConstrained((Uint8TypeDefinition) leafType);
        } else if (leafType instanceof Uint16TypeDefinition) {
            return baseTypeIfNotConstrained((Uint16TypeDefinition) leafType);
        } else if (leafType instanceof Uint32TypeDefinition) {
            return baseTypeIfNotConstrained((Uint32TypeDefinition) leafType);
        } else if (leafType instanceof Uint64TypeDefinition) {
            return baseTypeIfNotConstrained((Uint64TypeDefinition) leafType);
        } else {
            // Other types cannot be constrained, return the base type
            return baseType;
        }
    }

    private static BinaryTypeDefinition baseTypeIfNotConstrained(final @NonNull BinaryTypeDefinition type) {
        return baseTypeIfNotConstrained(type, type.getBaseType());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final @NonNull DecimalTypeDefinition type) {
        return baseTypeIfNotConstrained(type, type.getBaseType());
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final @NonNull InstanceIdentifierTypeDefinition type) {
        final InstanceIdentifierTypeDefinition base = type.getBaseType();
        return type.requireInstance() == base.requireInstance() ? base : type;
    }

    private static TypeDefinition<?> baseTypeIfNotConstrained(final @NonNull StringTypeDefinition type) {
        final StringTypeDefinition base = type.getBaseType();
        final List<PatternConstraint> patterns = type.getPatternConstraints();
        final Optional<LengthConstraint> optLengths = type.getLengthConstraint();

        if ((patterns.isEmpty() || patterns.equals(base.getPatternConstraints()))
                && (optLengths.isEmpty() || optLengths.equals(base.getLengthConstraint()))) {
            return base;
        }

        return type;
    }

    private static <T extends RangeRestrictedTypeDefinition<T, ?>> T baseTypeIfNotConstrained(final @NonNull T type) {
        return baseTypeIfNotConstrained(type, type.getBaseType());
    }

    private static <T extends RangeRestrictedTypeDefinition<T, ?>> T baseTypeIfNotConstrained(final @NonNull T type,
            final T base) {
        final Optional<?> optConstraint = type.getRangeConstraint();
        if (optConstraint.isEmpty()) {
            return base;
        }
        return optConstraint.equals(base.getRangeConstraint()) ? base : type;
    }

    private static <T extends LengthRestrictedTypeDefinition<T>> T baseTypeIfNotConstrained(final @NonNull T type,
            final T base) {
        final Optional<LengthConstraint> optConstraint = type.getLengthConstraint();
        if (optConstraint.isEmpty()) {
            return base;
        }
        return optConstraint.equals(base.getLengthConstraint()) ? base : type;
    }
}
