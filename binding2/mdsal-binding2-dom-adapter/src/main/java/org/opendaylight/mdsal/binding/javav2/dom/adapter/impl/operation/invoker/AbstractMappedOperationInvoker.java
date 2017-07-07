/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
abstract class AbstractMappedOperationInvoker<T> extends OperationServiceInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMappedOperationInvoker.class);
    private final Map<T, OperationMethodInvoker> map;

    protected AbstractMappedOperationInvoker(final Map<T, Method> map) {
        final Builder<T, OperationMethodInvoker> b = ImmutableMap.builder();

        for (final Entry<T, Method> e : map.entrySet()) {
            if (BindingReflections.isOperationMethod(e.getValue())) {
                b.put(e.getKey(), OperationMethodInvoker.from(e.getValue()));
            } else {
                LOG.debug("Method {} is not an operation method, ignoring it", e.getValue());
            }
        }

        this.map = b.build();
    }

    protected abstract T qnameToKey(QName qname);

    @Override
    public final void invoke(@Nonnull final Rpc<?, ?> impl, @Nonnull final QName operationName,
            @Nullable final TreeNode input, final RpcCallback<?> callback) {

        Preconditions.checkNotNull(impl, "Implementation must be supplied");

        final OperationMethodInvoker invoker = map.get(qnameToKey(operationName));
        Preconditions.checkArgument(invoker != null, "Supplied operation is not valid for implementation %s", impl);
        invoker.invokeOn(impl, input, callback);
    }
}
