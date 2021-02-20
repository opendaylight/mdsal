/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code input} statement.
 */
class InputGenerator extends AbstractCompositeGenerator<InputEffectiveStatement> {
    static final class Rpc extends InputGenerator {
        private final RpcGenerator rpc;

        Rpc(final InputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
                final RpcGenerator rpc) {
            super(statement, parent);
            this.rpc = requireNonNull(rpc);
        }

        @Override
        Member createMember() {
            return getParent().domain().addSecondary(rpc.ensureMember(), BindingMapping.RPC_INPUT_SUFFIX);
        }
    }

    InputGenerator(final InputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().getIdentifier());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        // FIXME: implement this

        return builder.build();
    }
}
