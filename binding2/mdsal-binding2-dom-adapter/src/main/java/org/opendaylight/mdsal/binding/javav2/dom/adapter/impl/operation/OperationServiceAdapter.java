/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
abstract class OperationServiceAdapter implements InvocationHandler {

    private final Entry<Method, OprInvocationStrategy> strategyEntry;
    private final Class<? extends Operation> type;
    private final BindingToNormalizedNodeCodec codec;
    private final DOMOperationService delegate;
    private final Operation proxy;

    OperationServiceAdapter(final Class<? extends Operation> type, final BindingToNormalizedNodeCodec codec,
                            final DOMOperationService domService) {
        this.type = Preconditions.checkNotNull(type);
        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(domService);
        this.strategyEntry = getOperationMethodStrategy(type);
        this.proxy = (Operation) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, this);
    }

    private static boolean isObjectMethod(final Method method) {
        switch (method.getName()) {
            case "toString":
                return method.getReturnType().equals(String.class) && method.getParameterTypes().length == 0;
            case "hashCode":
                return method.getReturnType().equals(int.class) && method.getParameterTypes().length == 0;
            case "equals":
                return method.getReturnType().equals(boolean.class) && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0] == Object.class;
            default:
                return false;
        }
    }

    abstract Entry<Method, OprInvocationStrategy> getOperationMethodStrategy(Class<?> type);

    protected BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    public DOMOperationService getDelegate() {
        return delegate;
    }

    Operation getProxy() {
        return proxy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final OprInvocationStrategy strategy = strategyEntry.getValue();

        if (method.equals(strategyEntry.getKey())) {
            strategy.invoke(args);
            return null;
        }

        if (isObjectMethod(method)) {
            return callObjectMethod(proxy, method, args);
        }
        throw new UnsupportedOperationException("Method " + method.toString() + "is unsupported.");
    }

    private Object callObjectMethod(final Object self, final Method method, final Object[] args) {
        switch (method.getName()) {
            case "toString":
                return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
            case "hashCode":
                return System.identityHashCode(self);
            case "equals":
                return self == args[0];
            default:
                return null;
        }
    }

    protected abstract class OprInvocationStrategy {

        private final SchemaPath oprSchemaPath;

        protected OprInvocationStrategy(final SchemaPath path) {
            oprSchemaPath = path;
        }

        abstract CompletionStage<DOMRpcResult> invokeOperation(Object[] args);

        abstract NormalizedNode<?, ?> serialize(TreeNode input);

        final SchemaPath getOperationPath() {
            return oprSchemaPath;
        }

        void invoke(final Object[] args) {
            final RpcCallback<?> callback = (RpcCallback<?>) args[args.length - 1];
            CompletionStage<DOMRpcResult> completionStage = invokeOperation(args);
            completionStage.whenCompleteAsync((result, throwable) -> invokeComplete(result, throwable, callback),
                MoreExecutors.directExecutor());
        }

        @SuppressWarnings("unchecked")
        <T> void invokeComplete(final DOMRpcResult domRpcResult, final Throwable throwable,
                final RpcCallback<T> rpcCallback) {
            if (throwable != null) {
                rpcCallback.onFailure(throwable);
            } else {
                final NormalizedNode<?, ?> domData = domRpcResult.getResult();
                final TreeNode bindingResult;
                if (domData != null) {
                    if (domData instanceof LazySerializedContainerNode) {
                        bindingResult = ((LazySerializedContainerNode) domData).bindingData();
                    } else {
                        final SchemaPath oprOutput = getOperationPath().createChild(
                            QName.create(getOperationPath().getLastComponent(), "output"));
                        bindingResult = codec.fromNormalizedNodeOperationData(oprOutput, (ContainerNode) domData);
                    }
                } else {
                    bindingResult = null;
                }
                rpcCallback.onSuccess((T) bindingResult);
            }
        }
    }
}