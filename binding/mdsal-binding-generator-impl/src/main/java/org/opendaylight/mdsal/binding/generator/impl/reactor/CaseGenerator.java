/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code case} statement.
 */
public final class CaseGenerator extends AbstractCompositeGenerator<CaseEffectiveStatement> {
    CaseGenerator(final CaseEffectiveStatement statement) {
        super(statement);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }
}
