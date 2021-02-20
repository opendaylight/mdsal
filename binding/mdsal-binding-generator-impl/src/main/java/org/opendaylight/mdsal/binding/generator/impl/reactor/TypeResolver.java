/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TypeResolver {
    static final class Derived extends TypeResolver {
        private final TypedefGenerator base;

        Derived(final TypeEffectiveStatement<?> type, final TypedefGenerator base) {
            super(type);
            this.base = requireNonNull(base);
            base.addDerivedResolver(this);
        }

        @Override
        void bindTypeDefinition(final GeneratorContext context) {
            // No-op, we'll be bound through onBaseTypeBound
        }
    }

    static final class Root extends TypeResolver {
        Root(final TypeEffectiveStatement<?> type) {
            super(type);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Root.class);

    private final TypeEffectiveStatement<?> type;

    /**
     * The generator corresponding to the type we are referencing. It can be one of these values:
     * <ul>
     *   <li>{@code this}, if this is a normal type</li>
     *   <li>{@code null}, if this is a {@code leafref} inside a {@code grouping} and the referenced type cannot be
     *       resolved because the reference points outside of the grouping</li>
     *   <li>a {@link TypedefGenerator} reference, if this is a {@code leafref}</li>
     *   <li>a {@link List} of {@link IdentityGenerator}s if this is an {@code identityref}</li>
     * </ul>
     */
    private Object referenced;

    TypeResolver(final TypeEffectiveStatement<?> type) {
        this.type = requireNonNull(type);
    }

    void bindTypeDefinition(final GeneratorContext context) {
        if (TypeDefinitions.LEAFREF.equals(type.argument())) {
            referenced = context.resolveLeafref(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow());
        } else if (TypeDefinitions.IDENTITYREF.equals(type.argument())) {
            referenced = type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                .map(BaseEffectiveStatement::argument)
                .map(context::resolveIdentity)
                .collect(Collectors.toUnmodifiableList());
        } else {
            referenced = this;
        }
        LOG.info("Resolved {} to generator {}", type, referenced);
    }

    final void onBaseTypeBound(final TypeResolver boundType) {
        LOG.info("Resolved {} with {}", type, boundType);
        if (boundType.referenced instanceof TypeResolver) {
            referenced = this;
        } else {
            referenced = boundType.referenced;
        }
    }
}
