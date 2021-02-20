/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code choice} statement.
 */
public final class ChoiceGenerator extends AbstractCompositeGenerator<ChoiceEffectiveStatement> {
    ChoiceGenerator(final ChoiceEffectiveStatement statement) {
        super(statement);
    }

    @Override
    boolean pushToDataTree(final SchemaInferenceStack dataTree) {
        return false;
    }
}
