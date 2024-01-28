/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;
import static org.opendaylight.yangtools.yang.common.YangConstants.operationInputQName;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class ActionAdapter extends AbstractBindingAdapter<DOMActionService> implements InvocationHandler {
    private final @NonNull ActionSpec<?, ?> spec;
    private final @NonNull NodeIdentifier inputName;
    private final @NonNull Absolute actionPath;

    ActionAdapter(final AdapterContext codec, final DOMActionService delegate, final ActionSpec<?, ?> spec) {
        super(codec, delegate);
        this.spec = requireNonNull(spec);
        actionPath = currentSerializer().getActionPath(spec);
        inputName = NodeIdentifier.create(operationInputQName(actionPath.lastNodeIdentifier().getModule()));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object [] args) throws Throwable {
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
                    return spec.type().getName() + "$Adapter{delegate=" + getDelegate() + "}";
                }
                break;
            case Naming.ACTION_INVOKE_NAME:
                if (args.length == 2) {
                    final var path = (InstanceIdentifier<?>) requireNonNull(args[0]);
                    checkArgument(!path.isWildcarded(), "Cannot invoke action on wildcard path %s", path);

                    final var input = (RpcInput) requireNonNull(args[1]);
                    final var serializer = currentSerializer();
                    final var future = getDelegate().invokeAction(actionPath,
                        DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
                            serializer.toYangInstanceIdentifier(path)),
                        serializer.toLazyNormalizedNodeActionInput(spec.type(), inputName, input));

                    // Invocation returned a future we know about -- return that future instead
                    if (ENABLE_CODEC_SHORTCUT && future.sourceFuture() instanceof BindingRpcFutureAware bindingAware) {
                        return bindingAware.getBindingFuture();
                    }

                    return Futures.transform(future, dom -> {
                        final var value = dom.value();
                        return RpcResultUtil.rpcResultFromDOM(dom.errors(), value == null ? null
                            : serializer.fromNormalizedNodeActionOutput(spec.type(), value));
                    }, MoreExecutors.directExecutor());
                }
                break;
            default:
                break;
        }

        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        throw new NoSuchMethodError("Method " + method.toString() + "is unsupported.");
    }
}
