/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
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

    private final BiFunction<D, BindingToNormalizedNodeCodec, B> bindingFactory;
    private final BindingToNormalizedNodeCodec codec;
    private final Class<B> bindingClass;

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
        final BindingToNormalizedNodeCodec codec, final BiFunction<D, BindingToNormalizedNodeCodec, B> bindingFactory) {
        super(ctx, domClass, null);
        this.bindingClass = requireNonNull(bindingClass);
        this.codec = requireNonNull(codec);
        this.bindingFactory = requireNonNull(bindingFactory);
    }

    @Override
    public void open(final boolean trackAllServices) {
        final String name = bindingClass.getName();
        LOG.debug("Starting tracker for {}", name);
        super.open(trackAllServices);
        LOG.debug("Tracker for {} started", name);
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
        final B binding = bindingFactory.apply(dom, codec);
        final Dict props = Dict.fromReference(reference);
        final ServiceRegistration<B> reg = context.registerService(bindingClass, binding, props);
        LOG.debug("Registered {} adapter {} of {} with {} as {}", bindingClass.getName(), binding, dom, props, reg);
        return reg;
    }

    @Override
    public void modifiedService(final ServiceReference<D> reference, final ServiceRegistration<B> service) {
        if (service != null) {
            final Dict newProps = reference != null ? Dict.fromReference(requireNonNull(reference)) : null;
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
