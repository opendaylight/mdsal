/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import java.util.function.Function;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
        extends AbstractAdaptingTracker<D, B, ServiceRegistration<B>> {
    private static final Logger LOG = LoggerFactory.getLogger(AdaptingTracker.class);

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
            final Function<D, B> bindingFactory) {
        super(ctx, domClass, bindingClass, bindingFactory);
    }

    @Override
    ServiceRegistration<B> addingService(final ServiceReference<D> reference, final D dom, final B binding) {
        final Dict props = Dict.fromReference(reference);
        final ServiceRegistration<B> reg = context.registerService(bindingClass, binding, props);
        LOG.debug("Registered {} adapter {} of {} with {} as {}", bindingClass.getName(), binding, dom, props, reg);
        return reg;
    }

    @Override
    void removedService(final ServiceRegistration<B> service) {
        service.unregister();
    }

    @Override
    void updatedService(final ServiceReference<D> reference, final ServiceRegistration<B> service) {
        final Dict newProps = Dict.fromReference(reference);
        LOG.debug("Updating service {} with properties {}", service, newProps);
        service.setProperties(newProps);
    }
}
