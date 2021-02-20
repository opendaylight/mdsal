/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common base class for {@link LeafGenerator} and {@link LeafListGenerator}.
 */
abstract class AbstractTypeAwareGenerator<T extends DataTreeEffectiveStatement<?>>
        extends AbstractTypeObjectGenerator<T> {
    AbstractTypeAwareGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        verify(statement instanceof TypeAware, "Unexpected statement %s", statement);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    final TypeDefinition<?> typeDefinition() {
        return ((TypeAware) statement()).getType();
    }
}
