/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

/**
 * Generator corresponding to a {@code leaf-list} statement.
 */
public final class LeafListGenerator extends AbstractTypeAwareGenerator<LeafListEffectiveStatement> {
    LeafListGenerator(final LeafListEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory, final TypedefGenerator baseGen,
            final TypeReference refType) {
        return Types.listTypeFor(super.methodReturnType(builderFactory, baseGen, refType));
    }
}
