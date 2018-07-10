/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMOperationCallback;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@NonNullByDefault
final class ActionAdapter extends AbstractBindingAdapter<DOMOperationService> implements InvocationHandler {
    private final Class<? extends Action<?, ?, ?>> type;
    private final Class<?> outputType;
    private final Class<?> inputType;

    private final NodeIdentifier inputName;
    private final SchemaPath schemaPath;

    ActionAdapter(final BindingToNormalizedNodeCodec codec, final DOMOperationService delegate,
            final Class<? extends Action<?, ?, ?>> type) {
        super(codec, delegate);
        this.type = requireNonNull(type);

        final DataSchemaNode pathSchema = getCodec().runtimeContext().getSchemaDefinition(type);
        verify(pathSchema instanceof ActionDefinition, "Type %s resolves to non-action schema %s", type, pathSchema);
        schemaPath = pathSchema.getPath();
        inputName = NodeIdentifier.create(YangConstants.operationInputQName(schemaPath.getLastComponent().getModule())
            .intern());

        final Type[] actionArgs = ClassLoaderUtils.findParameterizedType(type, Action.class).getActualTypeArguments();
        verify(actionArgs.length == 3);
        inputType = (Class<?>) actionArgs[1];
        outputType = (Class<?>) actionArgs[2];
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
                    final SettableFuture<Object> ret = SettableFuture.create();
                    getDelegate().invokeAction(schemaPath,
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
                            getCodec().toNormalized((InstanceIdentifier<DataObject>) args[0])),
                        LazySerializedContainerNode.create(inputName, (DataObject) args[1],
                            getCodec().getCodecRegistry()),
                        DOMOperationCallback.of(result -> ret.set(serializeResult(result)),
                            failure -> ret.setException(new IllegalStateException(failure))),
                        MoreExecutors.directExecutor());
                    return ret;
                }
                break;
        }

        throw new NoSuchMethodError("Method " + method.toString() + "is unsupported.");
    }

    private Object serializeResult(final DOMOperationResult result) {
        // FIXME: instantiate proper result
        return result;
    }
}
