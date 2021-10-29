/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultLeafRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.LeafRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

/**
 * Generator corresponding to a {@code leaf} statement.
 */
final class LeafGenerator extends AbstractTypeAwareGenerator<LeafEffectiveStatement, LeafRuntimeType, LeafGenerator> {
    LeafGenerator(final LeafEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    LeafRuntimeType createRuntimeType() {
        // FIXME: this is not quite right: even if we did not generate a type, we still want to create a runtime type
        return generatedType().map(type -> new DefaultLeafRuntimeType(type, statement())).orElse(null);
    }
}
