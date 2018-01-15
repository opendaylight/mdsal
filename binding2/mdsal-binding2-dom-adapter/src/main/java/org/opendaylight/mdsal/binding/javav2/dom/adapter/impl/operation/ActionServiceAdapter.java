/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
class ActionServiceAdapter implements InvocationHandler {

    private final Class<? extends Action<? extends TreeNode, ?, ?, ?>> type;
    private final BindingToNormalizedNodeCodec codec;
    private final DOMActionService delegate;
    private final Action<? extends TreeNode, ?, ?, ?> proxy;
    private final SchemaPath path;

    ActionServiceAdapter(final Class<? extends Action<? extends TreeNode, ?, ?, ?>> type,
            final BindingToNormalizedNodeCodec codec, final DOMActionService domService) {
        this.type = requireNonNull(type);
        this.codec = requireNonNull(codec);
        this.delegate = requireNonNull(domService);
        this.path = getCodec().getActionPath(type);
        proxy = (Action<? extends TreeNode, ?, ?, ?>) Proxy.newProxyInstance(type.getClassLoader(),
            new Class[] { type }, this);
    }

    public BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    public DOMActionService getDelegate() {
        return delegate;
    }

    Action<? extends TreeNode, ?, ?, ?> getProxy() {
        return proxy;
    }

    public Class<? extends Action<? extends TreeNode, ?, ?, ?>> getType() {
        return type;
    }

    public SchemaPath getPath() {
        return path;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public Object invoke(final Object proxy, final Method method, final Object[] args) {

        switch (method.getName()) {
            case "equals":
                if (args.length == 1) {
                    return proxy == args[0];
                }
                break;
            case "hashCode":
                if (args.length == 0) {
                    return System.identityHashCode(proxy);
                }
                break;
            case "toString":
                if (args.length == 0) {
                    return type.getName() + "$Adapter{delegate=" + getDelegate() + "}";
                }
                break;
            case "invoke":
                if (args.length == 3) {
                    final Input<?> input = (Input<?>) requireNonNull(args[0]);
                    final InstanceIdentifier<?> path = (InstanceIdentifier<?>) requireNonNull(args[1]);
                    final RpcCallback<Output> callback = (RpcCallback<Output>) requireNonNull(args[2]);

                    final FluentFuture<? extends DOMActionResult> future = getDelegate().invokeAction(getPath(),
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, getCodec().toNormalized(path)),
                        (ContainerNode) LazySerializedContainerNode.create(getPath(), (TreeNode) input,
                            getCodec().getCodecRegistry()));
                    //FIXME:this part is ugly, how to bridge FluentFuture and RpcCallback better?
                    // Invocation returned a future we know about -- return that future instead
                    if (future instanceof LazyDOMActionResultFuture) {
                        ListenableFuture<RpcResult<?>> bindingFuture =
                            ((LazyDOMActionResultFuture) future).getBindingFuture();
                        Futures.addCallback(bindingFuture, new FutureCallback<RpcResult<?>>() {

                            @Override
                            public void onSuccess(final RpcResult<?> result) {
                                if (result.isSuccessful()) {
                                    callback.onSuccess((Output) result.getResult());
                                } else {
                                    //FIXME: It's not suitable to do this way here. It's better for
                                    // 'onFailure' to accept Collection<RpcError> as input.
                                    result.getErrors().forEach(e -> callback.onFailure(e.getCause()));
                                }
                            }

                            @Override
                            public void onFailure(final Throwable throwable) {
                                callback.onFailure(throwable);
                            }
                        }, MoreExecutors.directExecutor());
                    } else {
                        Futures.addCallback(future, new FutureCallback<DOMActionResult>() {

                            @Override
                            public void onSuccess(final DOMActionResult result) {
                                if (result.getErrors().isEmpty()) {
                                    callback.onSuccess((Output) getCodec().fromNormalizedNodeOperationData(getPath(),
                                        result.getOutput().get()));
                                } else {
                                    result.getErrors().forEach(e -> callback.onFailure(e.getCause()));
                                }
                            }

                            @Override
                            public void onFailure(final Throwable throwable) {
                                callback.onFailure(throwable);
                            }
                        }, MoreExecutors.directExecutor());
                    }
                }
                return 0;
            default:
                break;
        }

        throw new UnsupportedOperationException("Method " + method.toString() + "is unsupported.");
    }
}
