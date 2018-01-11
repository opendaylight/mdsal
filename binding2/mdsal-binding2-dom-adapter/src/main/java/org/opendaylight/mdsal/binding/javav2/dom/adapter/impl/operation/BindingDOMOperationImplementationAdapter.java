/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker.OperationServiceInvoker;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Operation implementation adapter.
 */
@Beta
public class BindingDOMOperationImplementationAdapter implements DOMOperationImplementation {

    private static final Cache<Class<? extends Operation>, OperationServiceInvoker> SERVICE_INVOKERS =
            CacheBuilder.newBuilder().weakKeys().build();
    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher
    // cost
    private static final int COST = 1;

    private final BindingNormalizedNodeCodecRegistry codec;
    private final OperationServiceInvoker invoker;
    private final Operation delegate;
    private final QName inputQname;

    <T extends Operation> BindingDOMOperationImplementationAdapter(final BindingNormalizedNodeCodecRegistry codec,
            final Class<T> type, final Map<SchemaPath, Method> localNameToMethod, final T delegate) {
        try {
            this.invoker = SERVICE_INVOKERS.get(type, () -> {
                final Map<QName, Method> map = new HashMap<>();
                for (final Entry<SchemaPath, Method> e : localNameToMethod.entrySet()) {
                    map.put(e.getKey().getLastComponent(), e.getValue());
                }

                return OperationServiceInvoker.from(map);
            });
        } catch (final ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }

        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(delegate);
        inputQname = QName.create(BindingReflections.getQNameModule(type), "input").intern();
    }

    @Override
    public void invokeOperation(@Nonnull DOMRpcIdentifier operation, @Nullable NormalizedNode<?, ?> input,
                @Nonnull BiConsumer<DOMRpcResult, DOMRpcException> callback) {
        final SchemaPath schemaPath = operation.getType();
        final TreeNode bindingInput = input != null ? deserialize(operation.getType(), input) : null;
        final InstanceIdentifier<?> parent;
        if (!operation.getContextReference().isEmpty()) {
            parent = codec.fromYangInstanceIdentifier(operation.getContextReference());
        } else {
            parent = null;
        }

        invoke(schemaPath, parent, bindingInput, new RpcCallback<Object>() {
            @Override
            public void onSuccess(Object output) {
                callback.accept(new DefaultDOMRpcResult(
                    LazySerializedContainerNode.create(schemaPath, (TreeNode) output, codec)), null);
            }

            @SuppressWarnings("checkstyle:IllegalThrowable")
            @Override
            public void onFailure(Throwable error) {
                callback.accept(null, new DOMRpcInvokeException("Invoke operation error.", error));
            }
        });
    }

    private class DOMRpcInvokeException extends DOMRpcException {

        private static final long serialVersionUID = 1L;

        protected DOMRpcInvokeException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public long invocationCost() {
        return COST;
    }

    private TreeNode deserialize(final SchemaPath rpcPath, final NormalizedNode<?, ?> input) {
        if (input instanceof LazySerializedContainerNode) {
            return ((LazySerializedContainerNode) input).bindingData();
        }
        final SchemaPath inputSchemaPath = rpcPath.createChild(inputQname);
        return codec.fromNormalizedNodeOperationData(inputSchemaPath, (ContainerNode) input);
    }

    private void invoke(final SchemaPath schemaPath, final InstanceIdentifier<?> parent, final TreeNode input,
            final RpcCallback<?> callback) {
        invoker.invoke(delegate, schemaPath.getLastComponent(), parent, input, callback);
    }

    private ListenableFuture<DOMRpcResult>
            transformResult(final ListenableFuture<RpcResult<?>> bindingResult) {
        return LazyDOMOperationResultFuture.create(codec, bindingResult);
    }
}
