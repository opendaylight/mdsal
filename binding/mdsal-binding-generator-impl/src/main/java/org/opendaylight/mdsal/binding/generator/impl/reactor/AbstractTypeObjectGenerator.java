/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}.
 */
abstract class AbstractTypeObjectGenerator<T extends EffectiveStatement<?, ?>> extends Generator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);

    /**
     * The generator corresponding to our YANG base type. It produces the superclass of our encapsulated type. If it is
     * {@code null}, this generator is the root of the hierarchy.
     */
    private TypedefGenerator baseGenerator = null;

    /**
     * The generator corresponding to the type we are referencing. It can be one of these values:
     * <ul>
     *   <li>{@code this}, if this is a normal type</li>
     *   <li>{@code null}, if this is a {@code leafref} inside a {@code grouping} and the referenced type cannot be
     *       resolved because the reference points outside of the grouping.</li>
     *   <li>the referenced YANG type's generator</li>
     * </ul>
     */
    private AbstractTypeObjectGenerator<?> referencedGenerator = null;

    AbstractTypeObjectGenerator(final T statement) {
        super(statement);
    }

    final void linkType(final GeneratorContext context) {
        final TypeDefinition<?> type = type();
        final TypeDefinition<?> baseType = type.getBaseType();
        if (baseType == null) {
            // Nothing to search for, we are done
            LOG.info("No base type in {}", type);
            return;
        }

        final TypedefGenerator found = context.resolveTypedef(baseType.getQName());
        LOG.info("Resolved base generator to {}", found);
        baseGenerator = found;
    }

    final void bindLeafref(final GeneratorContext context) {
        final TypeDefinition<?> type = type();
        if (type instanceof LeafrefTypeDefinition) {
            referencedGenerator = context.resolveLeafref(((LeafrefTypeDefinition) type).getPathStatement());
        } else if (type instanceof IdentityrefTypeDefinition) {
            // FIXME: implement this. This is a bit tricky, as there may be multiple references, which do not quite
            //        handle.
            throw new UnsupportedOperationException();
        } else {
            referencedGenerator = this;
        }
    }

    /**
     * Extract this generator's {@link TypeDefinition}.
     *
     * @return A {@link TypeDefinition}
     */
    abstract @NonNull TypeDefinition<?> type();
}
