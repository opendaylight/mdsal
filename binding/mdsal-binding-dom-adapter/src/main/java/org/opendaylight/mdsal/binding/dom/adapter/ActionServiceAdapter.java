/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Proxy;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@NonNullByDefault
final class ActionServiceAdapter
        extends AbstractBindingLoadingAdapter<DOMActionService, ActionSpec<?, ?>, ActionAdapter>
        implements ActionService {
    private static final class Builder extends BindingDOMAdapterBuilder<ActionService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<? extends Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMActionService.class);
        }

        @Override
        protected ActionService createInstance(final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new ActionServiceAdapter(adapterContext(), delegates.getInstance(DOMActionService.class));
        }
    }

    static final Factory<ActionService> BUILDER_FACTORY = Builder::new;

    ActionServiceAdapter(final AdapterContext adapterContext, final DOMActionService delegate) {
        super(adapterContext, delegate);
    }

    @Override
    public <P extends DataObject, A extends Action<? extends InstanceIdentifier<P>, ?, ?>> A getActionHandle(
            final ActionSpec<A, P> spec, final Set<DataTreeIdentifier<P>> nodes) {
        final var type = spec.type();
        final var adapter = getAdapter(spec);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type },
            nodes.isEmpty() ? adapter : new ActionAdapterFilter(adapter, Set.copyOf(nodes))));
    }

    @Override
    ActionAdapter loadAdapter(final ActionSpec<?, ?> key) {
        final var type = key.type();
        checkArgument(BindingReflections.isBindingClass(type));
        checkArgument(type.isInterface(), "Supplied Action type must be an interface.");
        return new ActionAdapter(adapterContext(), getDelegate(), key);
    }
}
