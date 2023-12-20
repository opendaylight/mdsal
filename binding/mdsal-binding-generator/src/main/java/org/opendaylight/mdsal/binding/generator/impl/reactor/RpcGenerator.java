/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultRpcRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.RpcArchetype;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * Generator corresponding to a {@code rpc} statement.
 */
final class RpcGenerator extends AbstractInvokableGenerator<RpcEffectiveStatement, RpcRuntimeType> {
    RpcGenerator(final RpcEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.RPC;
    }

    @Override
    ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    ParameterizedType implementedType( final GeneratedType input, final GeneratedType output) {
        return BindingTypes.rpc(input, output);
    }

    @Override
    RpcArchetype createTypeImpl() {
        return new RpcArchetype(typeName(), statement(),
            getChild(this, InputEffectiveStatement.class).getOriginal().getGeneratedType().typeName(),
            getChild(this, OutputEffectiveStatement.class).getOriginal().getGeneratedType().typeName());
    }

    @Override
    CompositeRuntimeTypeBuilder<RpcEffectiveStatement, RpcRuntimeType> createBuilder(
            final RpcEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            RpcRuntimeType build(final GeneratedType generatedType, final RpcEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultRpcRuntimeType(generatedType, statement, childTypes);
            }
        };
    }
}
