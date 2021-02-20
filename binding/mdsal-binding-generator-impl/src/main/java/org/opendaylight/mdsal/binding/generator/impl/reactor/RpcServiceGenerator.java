/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;

final class RpcServiceGenerator extends AbstractImplicitGenerator {
    private final List<RpcGenerator> rpcs;

    RpcServiceGenerator(final ModuleGenerator parent, final List<RpcGenerator> rpcs) {
        super(parent);
        this.rpcs = requireNonNull(rpcs);
    }

    @Override
    String suffix() {
        return BindingMapping.RPC_SERVICE_SUFFIX;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.RPC_SERVICE);


//        addCodegenInformation(interfaceBuilder, module, "RPCs", rpcDefinitions);

        for (final RpcGenerator rpc : rpcs) {
            // FIXME: implement this
            // final String rpcName = BindingMapping.getClassName(rpc.getQName());
            // final String rpcMethodName = BindingMapping.getRpcMethodName(rpc.getQName());
            // final MethodSignatureBuilder method = builder.addMethod(rpcMethodName);
            //
            // // Do not refer to annotation class, as it may not be available at runtime
            // method.addAnnotation(CHECK_RETURN_VALUE_ANNOTATION);
            // addRpcMethodComment(method, rpc);
            // method.addParameter(
            //     createRpcContainer(context, rpcName, rpc, verifyNotNull(rpc.getInput()), RPC_INPUT), "input");
            // method.setReturnType(listenableFutureTypeFor(
            //     rpcResult(createRpcContainer(context, rpcName, rpc, verifyNotNull(rpc.getOutput()), RPC_OUTPUT))));
        }

        return builder.build();
    }
}
