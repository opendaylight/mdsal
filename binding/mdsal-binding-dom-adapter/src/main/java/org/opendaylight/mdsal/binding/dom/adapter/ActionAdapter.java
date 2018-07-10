/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationCallback;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@NonNullByDefault
final class ActionAdapter extends AbstractBindingAdapter<DOMOperationService> implements InvocationHandler {
    private final Class<? extends Action<?, ?, ?>> type;
    private final SchemaPath schemaPath;

    ActionAdapter(final BindingToNormalizedNodeCodec codec, final DOMOperationService delegate,
            final Class<? extends Action<?, ?, ?>> type) {
        super(codec, delegate);
        this.type = requireNonNull(type);
        this.schemaPath = getCodec().getActionPath(type);
    }

    @Override
    public @Nullable Object invoke(final @Nullable Object proxy, final @Nullable Method method,
            final Object @Nullable [] args) throws Throwable {
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
                if (args.length == 2) {
                    final InstanceIdentifier<?> path = (InstanceIdentifier<?>) requireNonNull(args[0]);
                    final DataObject input = (DataObject) requireNonNull(args[1]);

                    final SettableFuture<Object> ret = SettableFuture.create();

                    getDelegate().invokeAction(schemaPath,
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, getCodec().toNormalized(path)),
                        serializeInput(input),
                        DOMOperationCallback.of(result -> ret.set(serializeResult(result)),
                            failure -> ret.setException(new IllegalStateException(failure))),
                        MoreExecutors.directExecutor());
                    return ret;
                }
                break;
        }

        throw new NoSuchMethodError("Method " + method.toString() + "is unsupported.");
    }

    private ContainerNode serializeInput(DataObject input) {
        return LazySerializedContainerNode.create(inputName, input, getCodec().getCodecRegistry()),
    }

    private Object serializeResult(final DOMOperationResult result) {
        // FIXME: instantiate proper result
        return result;
    }
}
