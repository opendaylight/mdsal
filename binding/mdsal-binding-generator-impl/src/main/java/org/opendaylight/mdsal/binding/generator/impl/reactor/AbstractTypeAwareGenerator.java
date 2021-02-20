/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link LeafGenerator} and {@link LeafListGenerator}.
 */
abstract class AbstractTypeAwareGenerator<T extends DataTreeEffectiveStatement<?>>
        extends AbstractLeafrefAwareGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeAwareGenerator.class);

    AbstractTypeAwareGenerator(final T statement) {
        super(statement);
        verify(statement instanceof TypeAware, "Unexpected statement %s", statement);
    }

    final void linkType(final GeneratorContext context) {
        final TypeDefinition<?> type = ((TypeAware) statement()).getType();
        final TypeDefinition<?> baseType = type.getBaseType();
        if (baseType == null) {
            linkBaseType(context, type);
        } else {
            linkDerivedType(context, type, baseType);
        }
    }

    final void linkBaseType(final GeneratorContext context, final TypeDefinition<?> type) {
        if (YangConstants.RFC6020_YANG_NAMESPACE.equals(type.getQName().getNamespace())) {
            LOG.info("Linking base YANG type {}", type);

            // FIXME: implement this
            return;
        }

        LOG.info("Linking base type {}", type);

        // FIXME: implement this
    }

    final void linkDerivedType(final GeneratorContext context, final TypeDefinition<?> type,
            final TypeDefinition<?> base) {
        LOG.info("Linking derived type {} base {}", type, base);
        final TypedefGenerator baseGen = context.resolveTypedef(base.getQName());
        // FIXME: implement this
    }
}
