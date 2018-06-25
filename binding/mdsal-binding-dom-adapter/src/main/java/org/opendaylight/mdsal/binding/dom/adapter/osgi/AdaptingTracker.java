/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.osgi.framework.ServiceRegistration;
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
        extends ServiceTracker<D, ServiceRegistration<B>> {
    private static final Logger LOG = LoggerFactory.getLogger(AdaptingTracker.class);

    private final Function<D, B> bindingFactory;
    private final Class<B> bindingClass;

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
        final Function<D, B> bindingFactory) {
        super(ctx, domClass, null);
        this.bindingClass = requireNonNull(bindingClass);
        this.bindingFactory = requireNonNull(bindingFactory);
    }

    @Override
    public void open(final boolean trackAllServices) {
        LOG.debug("Starting tracker for {}", bindingClass.getName());
        super.open(trackAllServices);
        LOG.debug("Tracker for {} started", bindingClass.getName());
    }

    @Override
    public ServiceRegistration<B> addingService(final ServiceReference<D> reference) {
        if (reference == null) {
            LOG.debug("Null reference for {}, ignoring it", bindingClass.getName());
            return null;
        }
        final D dom = context.getService(reference);
        if (dom == null) {
            LOG.debug("Could not get {} service from {}, ignoring it", bindingClass.getName(), reference);
            return null;
        }
        final B binding = bindingFactory.apply(dom);
        final Dict props = Dict.fromReference(reference);
        final ServiceRegistration<B> reg = context.registerService(bindingClass, binding, props);
        LOG.debug("Registered {} adapter {} of {} with {} as {}", bindingClass.getName(), binding, dom, props, reg);
        return reg;
    }

    @Override
    public void modifiedService(final ServiceReference<D> reference, final ServiceRegistration<B> service) {
        if (service != null && reference != null) {
            final Dict newProps = Dict.fromReference(reference);
            LOG.debug("Updating service {} with properties {}", service, newProps);
            service.setProperties(newProps);
        }
    }

    @Override
    public void removedService(final ServiceReference<D> reference, final ServiceRegistration<B> service) {
        if (service != null) {
            context.ungetService(reference);
            service.unregister();
            LOG.debug("Unregistered service {}", service);
        }
    }
}
