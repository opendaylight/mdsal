/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
class ActionServiceAdapter extends OperationServiceAdapter {

    ActionServiceAdapter(final Class<? extends Operation> type, final BindingToNormalizedNodeCodec codec,
            final DOMOperationService domService) {
        super(type, codec, domService);
    }

    @Override
    protected Entry<Method, OprInvocationStrategy> getOperationMethodStrategy(Class<?> type) {
        final Entry<Method, OperationDefinition> methodToSchema = getCodec().getOprMethodToSchema(type);

        return new SimpleEntry<>(methodToSchema.getKey(), createStrategy((ActionDefinition) methodToSchema.getValue()));
    }

    private OprInvocationStrategy createStrategy(final ActionDefinition schema) {
        return new ActionStrategy(schema.getPath());
    }

    protected final class ActionStrategy extends OprInvocationStrategy {
        ActionStrategy(final SchemaPath path) {
            super(path);
        }

        @Override
        NormalizedNode<?, ?> serialize(final TreeNode input) {
            return LazySerializedContainerNode.create(getOperationPath(), input, getCodec().getCodecRegistry());
        }

        @Override
        CompletionStage<DOMRpcResult> invokeOperation(final Object[] args) {
            if (args.length != 3) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            final YangInstanceIdentifier ii = getCodec().toYangInstanceIdentifier((InstanceIdentifier<?>) args[1]);
            return invoke0(getOperationPath(), ii, serialize((TreeNode) args[0]));
        }

        private CompletionStage<DOMRpcResult> invoke0(final SchemaPath schemaPath, final YangInstanceIdentifier parent,
                final NormalizedNode<?, ?> input) {
            CompletableFuture<DOMRpcResult> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                getDelegate().invokeAction(schemaPath, parent, input, (result, cause) -> {
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

}