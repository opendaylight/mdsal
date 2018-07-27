/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FluentFuture;
import java.lang.reflect.Proxy;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Beta
@NonNullByDefault
final class ActionServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMOperationService, Class<? extends Action<?, ?, ?>>, ActionAdapter>
        implements ActionService {
    private static final class ConstrainedAction implements Delegator<Action<?, ?, ?>>,
            Action<InstanceIdentifier<?>, RpcInput, RpcOutput> {
        private final Action<InstanceIdentifier<?>, RpcInput, RpcOutput> delegate;
        private final Set<? extends DataTreeIdentifier<?>> nodes;

        ConstrainedAction(final Action<?, ?, ?> delegate, final Set<? extends DataTreeIdentifier<?>> nodes) {
            this.delegate = requireNonNull((Action) delegate);
            this.nodes = requireNonNull(nodes);
        }

        @Override
        public FluentFuture<RpcResult<RpcOutput>> invoke(final InstanceIdentifier<?> path, final RpcInput input) {
            checkState(nodes.contains(path), "Cannot service %s", path);
            return delegate.invoke(path, input);
        }

        @Override
        public Action<?, ?, ?> getDelegate() {
            return delegate;
        }
    }

    ActionServiceAdapter(final BindingToNormalizedNodeCodec codec, final DOMOperationService delegate) {
        super(codec, delegate);
    }

    @Override
    public <O extends DataObject, T extends Action<?, ?, ?>> T getActionHandle(final Class<T> actionInterface,
            final Set<DataTreeIdentifier<O>> nodes) {
        return !nodes.isEmpty() ? (T) new ConstrainedAction(getActionHandle(actionInterface, ImmutableSet.of()), nodes)
                : (T) Proxy.newProxyInstance(actionInterface.getClassLoader(), new Class[] { actionInterface },
                    getAdapter(actionInterface));
    }

    @Override
    ActionAdapter loadAdapter(final Class<? extends Action<?, ?, ?>> key) {
        checkArgument(BindingReflections.isBindingClass(key));
        checkArgument(key.isInterface(), "Supplied Action type must be an interface.");
        return new ActionAdapter(getCodec(), getDelegate(), key);
    }
}
