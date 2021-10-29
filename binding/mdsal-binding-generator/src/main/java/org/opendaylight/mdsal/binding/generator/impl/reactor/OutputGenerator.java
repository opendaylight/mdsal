/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultOutputRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * Generator corresponding to an {@code input} statement.
 */
class OutputGenerator
        extends OperationContainerGenerator<OutputEffectiveStatement, OutputRuntimeType, OutputGenerator> {
    OutputGenerator(final OutputEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent, BindingTypes.RPC_OUTPUT);
    }

    @Override
    final OutputRuntimeType createRuntimeType(final GeneratedType type,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children, final List<AugmentRuntimeType> augments) {
        return new DefaultOutputRuntimeType(type, statement(), children, augments);
    }
}
