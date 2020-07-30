/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

final class AdaptingComponentTracker<D extends DOMService, B extends BindingService>
        extends AbstractAdaptingTracker<D, B, AdaptingComponentTracker.ComponentHolder<B>> {
    static final class ComponentHolder<B extends BindingService> {
        final B binding;
        ComponentInstance component;

        ComponentHolder(final B binding, final ComponentInstance component) {
            this.binding = requireNonNull(binding);
            this.component = requireNonNull(component);
        }
    }

    private final ComponentFactory componentFactory;

    AdaptingComponentTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
            final Function<D, B> bindingFactory, final ComponentFactory componentFactory) {
        super(ctx, domClass, bindingClass, bindingFactory);
        this.componentFactory = requireNonNull(componentFactory);
    }

    @Override
    ComponentHolder<B> addingService(final ServiceReference<D> reference, final D dom, final B binding) {
        return new ComponentHolder<>(binding, componentFactory.newInstance(Dict.fromReference(reference, binding)));
    }

    @Override
    void removedService(final ComponentHolder<B> service) {
        service.component.dispose();
    }

    @Override
    void updatedService(final ServiceReference<D> reference, final ComponentHolder<B> service) {
        service.component.dispose();
        service.component = componentFactory.newInstance(Dict.fromReference(reference, service.binding));
    }
}
