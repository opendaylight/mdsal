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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ServiceTracker which adapts a DOMService to a BindingService.
 *
 * @param <D> DOMService type
 * @param <B> BindingService type
 * @author Robert Varga
 */
final class AdaptingTracker<D extends DOMService, B extends BindingService>
        extends ServiceTracker<D, AdaptingTracker.ComponentHolder<B>> {
    static final class ComponentHolder<B extends BindingService> {
        final B binding;
        ComponentInstance component;

        ComponentHolder(final B binding, final ComponentInstance component) {
            this.binding = requireNonNull(binding);
            this.component = requireNonNull(component);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AdaptingTracker.class);

    private final Function<D, B> bindingFactory;
    private final @NonNull Class<B> bindingClass;
    private final ComponentFactory componentFactory;

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
            final Function<D, B> bindingFactory, final ComponentFactory componentFactory) {
        super(ctx, domClass, null);
        this.bindingClass = requireNonNull(bindingClass);
        this.bindingFactory = requireNonNull(bindingFactory);
        this.componentFactory = requireNonNull(componentFactory);
    }

    @Override
    public void open(final boolean trackAllServices) {
        LOG.debug("Starting tracker for {}", bindingClass.getName());
        super.open(trackAllServices);
        LOG.debug("Tracker for {} started", bindingClass.getName());
    }

    @Override
    public ComponentHolder<B> addingService(final ServiceReference<D> reference) {
        if (reference == null) {
            LOG.debug("Null reference for {}, ignoring it", bindingClass.getName());
            return null;
        }
        if (reference.getProperty(ServiceProperties.IGNORE_PROP) != null) {
            LOG.debug("Ignoring reference {} due to {}", reference, ServiceProperties.IGNORE_PROP);
            return null;
        }

        final D dom = context.getService(reference);
        if (dom == null) {
            LOG.debug("Could not get {} service from {}, ignoring it", bindingClass.getName(), reference);
            return null;
        }

        final B binding = bindingFactory.apply(dom);
        return new ComponentHolder<>(binding, componentFactory.newInstance(Dict.fromReference(reference, binding)));
    }

    @Override
    public void modifiedService(final ServiceReference<D> reference, final ComponentHolder<B> service) {
        if (service != null && reference != null) {
            service.component.dispose();
            service.component = componentFactory.newInstance(Dict.fromReference(reference, service.binding));
        }
    }

    @Override
    public void removedService(final ServiceReference<D> reference, final ComponentHolder<B> service) {
        if (service != null) {
            context.ungetService(reference);
            service.component.dispose();
            LOG.debug("Unregistered service {}", service);
        }
    }
}
