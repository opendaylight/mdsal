/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.util.Dictionary;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
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
 */
final class AdaptingTracker<D extends DOMService<?, ?>, B extends BindingService>
        extends ServiceTracker<D, AdaptingTracker.AdaptedService<B>> {
    // Visible because of use in containing class type declaration
    static final class AdaptedService<B extends BindingService> {
        private final B delegate;

        private ComponentInstance<? extends B> component;

        private AdaptedService(final B delegate, final ComponentInstance<? extends B> component) {
            this.delegate = requireNonNull(delegate);
            this.component = requireNonNull(component);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AdaptingTracker.class);

    private final Function<D, B> bindingFactory;
    private final @NonNull Class<B> bindingClass;
    private final ComponentFactory<? extends B> componentFactory;

    AdaptingTracker(final BundleContext ctx, final Class<D> domClass, final Class<B> bindingClass,
            final Function<D, B> bindingFactory, final ComponentFactory<? extends B> componentFactory) {
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
    public void close() {
        LOG.debug("Stopping tracker for {}", bindingClass.getName());
        super.close();
        LOG.debug("Tracker for {} stopped", bindingClass.getName());
    }

    @Override
    public AdaptedService<B> addingService(final ServiceReference<D> reference) {
        if (reference == null) {
            LOG.debug("Null reference for {}, ignoring it", bindingClass.getName());
            return null;
        }
        if (reference.getProperty(ServiceProperties.IGNORE_PROP) != null) {
            LOG.debug("Ignoring reference {} due to {}", reference, ServiceProperties.IGNORE_PROP);
            return null;
        }

        final var dom = context.getService(reference);
        if (dom == null) {
            LOG.debug("Could not get {} service from {}, ignoring it", bindingClass.getName(), reference);
            return null;
        }

        final var binding = bindingFactory.apply(dom);
        final var service = new AdaptedService<>(binding,
            componentFactory.newInstance(referenceProperties(reference, binding)));
        LOG.debug("Registered {}", service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<D> reference, final AdaptedService<B> service) {
        if (service != null && reference != null) {
            LOG.debug("Restarting {}", service);
            service.component.dispose();
            service.component = componentFactory.newInstance(referenceProperties(reference, service.delegate));
            LOG.debug("Restarted {}", service);
        }
    }

    @Override
    public void removedService(final ServiceReference<D> reference, final AdaptedService<B> service) {
        if (service != null) {
            context.ungetService(reference);
            service.component.dispose();
            LOG.debug("Unregistered {}", service);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("bindingClass", bindingClass.getName()).toString();
    }

    static Dictionary<String, Object> referenceProperties(final ServiceReference<?> ref, final BindingService service) {
        final String[] keys = ref.getPropertyKeys();
        final Map<String, Object> props = Maps.newHashMapWithExpectedSize(keys.length + 1);
        for (String key : keys) {
            // Ignore properties with our prefix: we are not exporting those
            if (!key.startsWith(ServiceProperties.PREFIX)) {
                final var value = ref.getProperty(key);
                if (value != null) {
                    props.put(key, value);
                }
            }
        }

        // Second phase: apply any our properties
        for (String key : keys) {
            if (key.startsWith(ServiceProperties.OVERRIDE_PREFIX)) {
                final Object value = ref.getProperty(key);
                if (value != null) {
                    final String newKey = key.substring(ServiceProperties.OVERRIDE_PREFIX.length());
                    if (!newKey.isEmpty()) {
                        LOG.debug("Overriding property {}", newKey);
                        props.put(newKey, value);
                    }
                }
            }
        }

        props.put(AbstractAdaptedService.DELEGATE, service);
        return FrameworkUtil.asDictionary(props);
    }
}
