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
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
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
    void addImplementedType(final TypeBuilderFactory builderFactory, final GeneratedTypeBuilder builder,
            final GeneratedType input, final GeneratedType output) {
        builder.addImplementsType(BindingTypes.rpc(input, output));
        builder.addMethod(Naming.RPC_INVOKE_NAME).setAbstract(true)
            .addParameter(input, "input")
            .setReturnType(Types.listenableFutureTypeFor(BindingTypes.rpcResult(output)))
            .addAnnotation(OVERRIDE_ANNOTATION);
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
