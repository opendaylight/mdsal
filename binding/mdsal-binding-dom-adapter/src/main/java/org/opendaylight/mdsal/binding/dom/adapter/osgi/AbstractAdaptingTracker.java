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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
abstract class AbstractAdaptingTracker<D extends DOMService, B extends BindingService, T>
        extends ServiceTracker<D, T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdaptingTracker.class);

    private final Function<D, B> bindingFactory;
    final @NonNull Class<B> bindingClass;

    AbstractAdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
        final Function<D, B> bindingFactory) {
        super(ctx, domClass, null);
        this.bindingClass = requireNonNull(bindingClass);
        this.bindingFactory = requireNonNull(bindingFactory);
    }

    @Override
    public final void open(final boolean trackAllServices) {
        LOG.debug("Starting tracker for {}", bindingClass.getName());
        super.open(trackAllServices);
        LOG.debug("Tracker for {} started", bindingClass.getName());
    }

    @Override
    public final T addingService(final ServiceReference<D> reference) {
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

        return addingService(reference, dom, bindingFactory.apply(dom));
    }

    abstract @NonNull T addingService(@NonNull ServiceReference<D> reference, @NonNull D dom, @NonNull B binding);

    @Override
    public final void modifiedService(final ServiceReference<D> reference, final T service) {
        if (service != null && reference != null) {
            updatedService(reference, service);
        }
    }

    abstract void updatedService(@NonNull ServiceReference<D> reference, @NonNull T service);

    @Override
    public final void removedService(final ServiceReference<D> reference, final T service) {
        if (service != null) {
            context.ungetService(reference);
            removedService(service);
            LOG.debug("Unregistered service {}", service);
        }
    }

    abstract void removedService(@NonNull T service);

}
