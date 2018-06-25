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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

final class AdaptingTracker<D extends DOMService, B extends BindingService>
        extends ServiceTracker<D, ServiceRegistration<B>> {
    private final BiFunction<BindingToNormalizedNodeCodec, D, B> bindingFactory;
    private final BindingToNormalizedNodeCodec codec;
    private final Class<B> bindingClass;

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
        final BindingToNormalizedNodeCodec codec, final BiFunction<BindingToNormalizedNodeCodec, D, B> bindingFactory) {
        super(ctx, domClass, null);
        this.bindingClass = requireNonNull(bindingClass);
        this.codec = requireNonNull(codec);
        this.bindingFactory = requireNonNull(bindingFactory);
    }

    @Override
    public final ServiceRegistration<B> addingService(final @Nullable ServiceReference<D> reference) {
        return context.registerService(bindingClass, bindingFactory.apply(codec, context.getService(reference)),
            Dict.fromReference(reference));
    }

    @Override
    public final void modifiedService(final @Nullable ServiceReference<D> reference,
            final ServiceRegistration<B> service) {
        service.setProperties(Dict.fromReference(reference));
    }

    @Override
    public final void removedService(final @Nullable ServiceReference<D> reference,
            final ServiceRegistration<B> service) {
        service.unregister();
    }
}
