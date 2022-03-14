/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultOutputRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * Generator corresponding to an {@code input} statement.
 */
class OutputGenerator extends OperationContainerGenerator<OutputEffectiveStatement, OutputRuntimeType> {
    OutputGenerator(final OutputEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent, BindingTypes.RPC_OUTPUT);
    }

    @Override
    final OutputRuntimeType createExternalRuntimeType(final GeneratedType type,  final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments, final List<AugmentRuntimeType> referencingAugments) {
        return DefaultOutputRuntimeType.of(type, statement(), children, augments, referencingAugments);
    }

    @Override
    final OutputRuntimeType createInternalRuntimeType( final OutputEffectiveStatement statement,
            final GeneratedType type, final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
        return DefaultOutputRuntimeType.of(type, statement, children, augments);
    }
}
