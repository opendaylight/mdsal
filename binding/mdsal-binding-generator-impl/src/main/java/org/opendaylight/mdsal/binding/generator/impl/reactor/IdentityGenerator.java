/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code identity} statement.
 */
public final class IdentityGenerator extends Generator<IdentityEffectiveStatement> {
    IdentityGenerator(final IdentityEffectiveStatement statement) {
        super(statement);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.IDENTITY;
    }

    @Override
    boolean pushToDataTree(final SchemaInferenceStack dataTree) {
        throw new UnsupportedOperationException();
    }
}
