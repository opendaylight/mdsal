/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeResolver.Derived;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
public final class TypedefGenerator extends AbstractTypeObjectGenerator<TypedefEffectiveStatement> {
    // List of all resolver for types directly derived from this typedef. We populate this list during initial type
    // linking. It allows us to easily cascade inferences made by this typedef down the type derivation tree.
    private List<Derived> derivedResolvers = null;

    TypedefGenerator(final TypedefEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.TYPEDEF;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    @Override
    void onTypeResolverBound(final TypeResolver boundType) {
        if (derivedResolvers != null) {
            for (Derived derived : derivedResolvers) {
                derived.onBaseTypeBound(boundType);
            }
        }
    }

    void addDerivedResolver(final Derived derivedResolver) {
        if (derivedResolvers == null) {
            derivedResolvers = new ArrayList<>(4);
        }
        derivedResolvers.add(derivedResolver);
    }

    @Override
    TypeDefinition<?> typeDefinition() {
        return statement().getTypeDefinition();
    }
}
