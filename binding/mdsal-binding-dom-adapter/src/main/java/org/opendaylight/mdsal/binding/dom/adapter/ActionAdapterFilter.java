/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.contract.Naming;

final class ActionAdapterFilter implements InvocationHandler {
    private final Set<DataTreeIdentifier<?>> nodes;
    private final ActionAdapter delegate;

    ActionAdapterFilter(final ActionAdapter delegate, final Set<DataTreeIdentifier<?>> nodes) {
        this.delegate = requireNonNull(delegate);
        this.nodes = requireNonNull(nodes);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Naming.ACTION_INVOKE_NAME.equals(method.getName()) && args.length == 2) {
            final InstanceIdentifier<?> path = (InstanceIdentifier<?>) requireNonNull(args[0]);
            checkState(nodes.contains(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, path)),
                "Cannot service %s", path);
        }
        return delegate.invoke(proxy, method, args);
    }
}
