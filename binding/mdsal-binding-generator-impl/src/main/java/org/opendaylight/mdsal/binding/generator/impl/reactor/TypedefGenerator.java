/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
public final class TypedefGenerator extends AbstractTypeObjectGenerator<TypedefEffectiveStatement> {
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
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        // FIXME: implement this
        return null;
    }
}
