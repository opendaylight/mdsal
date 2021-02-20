/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}.
 */
abstract class AbstractTypeObjectGenerator<T extends EffectiveStatement<?, ?>> extends Generator<T> {
    private TypeResolver resolver;

    AbstractTypeObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    final void linkType(final GeneratorContext context) {
        final TypeEffectiveStatement<?> type = statement().findFirstEffectiveSubstatement(TypeEffectiveStatement.class)
            .orElseThrow();
        final QName typeName = type.argument();
        if (!YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule())) {
            resolver = new TypeResolver.Derived(type, context.resolveTypedef(typeName));
        } else {
            resolver = new TypeResolver.Root(type);
        }
    }

    final void bindTypeDefinition(final GeneratorContext context) {
        resolver.bindTypeDefinition(context);
        onTypeResolverBound(resolver);
    }

    void onTypeResolverBound(final TypeResolver boundType) {
        // No-op for most implementations
    }

    /**
     * Extract this generator's {@link TypeDefinition}.
     *
     * @return A {@link TypeDefinition}
     */
    abstract @NonNull TypeDefinition<?> typeDefinition();
}
