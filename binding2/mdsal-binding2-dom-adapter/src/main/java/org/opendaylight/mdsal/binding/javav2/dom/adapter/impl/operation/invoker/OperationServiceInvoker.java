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
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides single method invocation of operations on supplied instance.
 *
 * <p>
 * Operation Service invoker provides common invocation interface for any subtype of operation. via
 * {@link #invoke(Rpc, QName, TreeNode, RpcCallback)} method.
 */
@Beta
public abstract class OperationServiceInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(OperationServiceInvoker.class);

    /**
     * Creates OperationServiceInvoker for specified operation type.
     *
     * @param type
     *            operation interface, which was generated from model.
     * @return Cached instance of {@link OperationServiceInvoker} for supplied operation type.
     *
     */
    public static OperationServiceInvoker from(final Class<? extends Operation> type) {
        return ClassBasedOperationServiceInvoker.instanceFor(type);
    }

    /**
     * Creates an OperationServiceInvoker for specified QName-&lt;Method mapping.
     *
     * @param qnameToMethod
     *            translation mapping, must not be null nor empty.
     * @return An {@link OperationMethodInvoker} instance.
     */
    public static OperationServiceInvoker from(final Map<QName, Method> qnameToMethod) {
        Preconditions.checkArgument(!qnameToMethod.isEmpty());
        QNameModule module = null;

        for (final QName qname : qnameToMethod.keySet()) {
            if (module != null) {
                if (!module.equals(qname.getModule())) {
                    LOG.debug("QNames from different modules {} and {}, falling back to QName map", module,
                            qname.getModule());
                    return QNameOperationServiceInvoker.instanceFor(qnameToMethod);
                }
            } else {
                module = qname.getModule();
            }
        }

        // All module are equal, which means we can use localName only
        return LocalNameOperationServiceInvoker.instanceFor(module, qnameToMethod);
    }

    /**
     * Invokes supplied operation on provided implementation of Operation Service.
     *
     * @param impl
     *            Implementation on which operation should be invoked.
     * @param operationName
     *            Name of operation to be invoked.
     * @param input
     *            Input data for operation.
     */
    public abstract void invoke(@Nonnull Rpc<?, ?> impl, @Nonnull QName operationName, @Nullable TreeNode input,
            RpcCallback<?> callback);
}
