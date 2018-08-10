/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Superclass implementation adapter of RPC and Action/ListAction.
 */
@Beta
public abstract class AbstractBindingDOMImplementationAdapter<T extends Operation> {
    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher
    // cost
    private static final int COST = 1;

    private final BindingNormalizedNodeCodecRegistry codec;
    private final T delegate;
    private final QName inputQname;

    AbstractBindingDOMImplementationAdapter(final BindingNormalizedNodeCodecRegistry codec,
            final Class<? extends Operation> clazz, final T delegate) {
        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(delegate);
        inputQname = QName.create(BindingReflections.getQNameModule(clazz), "input").intern();
    }

    protected T getDelegate() {
        return delegate;
    }

    public BindingNormalizedNodeCodecRegistry getCodec() {
        return codec;
    }

    public long invocationCost() {
        return COST;
    }

    TreeNode deserialize(final SchemaPath path, final NormalizedNode<?, ?> input) {
        if (input instanceof LazySerializedContainerNode) {
            return ((LazySerializedContainerNode) input).bindingData();
        }
        final SchemaPath inputSchemaPath = path.createChild(inputQname);
        return codec.fromNormalizedNodeOperationData(inputSchemaPath, (ContainerNode) input);
    }

    FluentFuture<DOMRpcResult> transformResult(final ListenableFuture<RpcResult<?>> bindingResult) {
        return LazyDOMOperationResultFuture.create(codec, bindingResult);
    }
}
