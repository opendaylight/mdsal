/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}.
 */
abstract class AbstractTypeObjectGenerator<T extends EffectiveStatement<?, ?>> extends Generator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);

    // The generator corresponding to our YANG base type. It produces the superclass of our encapsulated type.
    private TypedefGenerator baseGenerator;

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
        verify(type == baseType, "Expected %s, found unexpected %s", baseType, found);
        baseGenerator = found;
    }

    final void bindLeafref(final GeneratorContext context) {
        // FIXME: implement this
    }

    abstract @NonNull TypeDefinition<?> type();
}
