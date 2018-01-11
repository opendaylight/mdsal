/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor.ContextReferenceExtractor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
class RpcServiceAdapter extends OperationServiceAdapter {

    RpcServiceAdapter(final Class<? extends Rpc<?, ?>> type, final BindingToNormalizedNodeCodec codec,
            final DOMOperationService domService) {
        super(type, codec, domService);
    }

    @Override
    protected Entry<Method, OprInvocationStrategy> getOperationMethodStrategy(Class<?> type) {
        final Entry<Method, OperationDefinition> methodToSchema = getCodec().getOprMethodToSchema(type);

        return new SimpleEntry<>(methodToSchema.getKey(), createStrategy(methodToSchema.getKey(),
            (RpcDefinition) methodToSchema.getValue()));
    }

    private OprInvocationStrategy createStrategy(final Method method, final RpcDefinition schema) {
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(schema);
        if (strategy.isContextBasedRouted()) {
            return new RoutedStrategy(schema.getPath(), method, strategy.getLeaf());
        }
        return new NonRoutedStrategy(schema.getPath());
    }


    private class NonRoutedStrategy extends OprInvocationStrategy {

        NonRoutedStrategy(final SchemaPath path) {
            super(path);
        }

        @Override
        NormalizedNode<?, ?> serialize(final TreeNode input) {
            return LazySerializedContainerNode.create(getOperationPath(), input, getCodec().getCodecRegistry());
        }

        @Override
        CompletableFuture<DOMRpcResult> invokeOperation(Object[] args) {
            if (args.length != 2) {
                throw new IllegalArgumentException("Input must be provided.");
            }

            return invoke0(getOperationPath(), serialize((TreeNode) args[0]));
        }

        private CompletableFuture<DOMRpcResult> invoke0(final SchemaPath schemaPath, final NormalizedNode<?, ?> input) {
            final CompletableFuture<DOMRpcResult> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                getDelegate().invokeRpc(schemaPath, input, (result, cause) -> {
                    if (cause != null) {
                        future.completeExceptionally(cause);
                    } else {
                        future.complete(result);
                    }
                });
            });
            return future;
        }
    }

    private final class RoutedStrategy extends NonRoutedStrategy {

        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        protected RoutedStrategy(final SchemaPath path, final Method rpcMethod, final QName leafName) {
            super(path);
            final Optional<Class<? extends Input<?>>> maybeInputType =
                    BindingReflections.resolveOperationInputClass(rpcMethod);
            Preconditions.checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends Input<?>> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            this.contextName = new NodeIdentifier(leafName);
        }

        @SuppressWarnings("rawtypes")
        @Override
        NormalizedNode<?, ?> serialize(final TreeNode input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            if (bindingII != null) {
                final YangInstanceIdentifier yangII = getCodec().toYangInstanceIdentifierCached(bindingII);
                final LeafNode contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(getOperationPath(), input, contextRef,
                    getCodec().getCodecRegistry());
            }
            return LazySerializedContainerNode.create(getOperationPath(), input, getCodec().getCodecRegistry());
        }

    }
}