/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks bundles and attempts to retrieve YangModuleInfo, which is then fed into ModuleInfoRegistry.
 */
final class YangModuleInfoScanner extends BundleTracker<List<ObjectRegistration<YangModuleInfo>>> {
    private static final Logger LOG = LoggerFactory.getLogger(YangModuleInfoScanner.class);
    // FIXME: this should be in a place shared with maven-sal-api-gen-plugin
    private static final String MODULE_INFO_PROVIDER_PATH_PREFIX = "META-INF/services/";

    private final YangModuleInfoRegistry moduleInfoRegistry;

    YangModuleInfoScanner(final BundleContext context, final YangModuleInfoRegistry moduleInfoRegistry) {
        super(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE, null);
        this.moduleInfoRegistry = requireNonNull(moduleInfoRegistry);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public List<ObjectRegistration<YangModuleInfo>> addingBundle(final Bundle bundle, final BundleEvent event) {
        if (bundle.getBundleId() == Constants.SYSTEM_BUNDLE_ID) {
            LOG.debug("Ignoring system bundle {}", bundle);
            return List.of();
        }

        // Load YangModuleInfos
        final var moduleInfos = loadBundleServices(bundle, YangModelBindingProvider.class).stream()
            .map(YangModelBindingProvider::getModuleInfo)
            .collect(Collectors.toUnmodifiableList());

        final List<ObjectRegistration<YangModuleInfo>> registrations = moduleInfoRegistry.registerInfos(moduleInfos);
        LOG.trace("Bundle {} resulted in registrations {}", bundle, registrations);
        moduleInfoRegistry.scannerUpdate();
        return registrations;
    }

    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event,
            final List<ObjectRegistration<YangModuleInfo>> regs) {
        if (bundle.getBundleId() == Constants.SYSTEM_BUNDLE_ID) {
            LOG.debug("Framework bundle {} got event {}", bundle, event.getType());
            if ((event.getType() & BundleEvent.STOPPING) != 0) {
                LOG.info("OSGi framework is being stopped, halting bundle scanning");
                moduleInfoRegistry.scannerShutdown();
            }
        }
    }

    @Override
    public void removedBundle(final Bundle bundle, final BundleEvent event,
            final List<ObjectRegistration<YangModuleInfo>> regs) {
        regs.forEach(ObjectRegistration::close);
        moduleInfoRegistry.scannerUpdate();
    }

    private static <T> List<T> loadBundleServices(final Bundle bundle, final Class<T> serviceClass) {
        final var serviceName = serviceClass.getName();
        final var serviceEntry = MODULE_INFO_PROVIDER_PATH_PREFIX + serviceName;
        final var resource = bundle.getEntry(serviceEntry);
        if (resource == null) {
            LOG.debug("Bundle {} does not have an entry for {}", bundle, serviceEntry);
            return List.of();
        }

        LOG.debug("Got addingBundle({}) with {} resource {}", bundle, serviceName, resource);
        final List<String> lines;
        try {
            lines = Resources.readLines(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error while reading {} from bundle {}", resource, bundle, e);
            return List.of();
        }

        if (lines.isEmpty()) {
            LOG.debug("Bundle {} has empty services for {}", bundle, serviceEntry);
            return List.of();
        }

        final var services = new ArrayList<T>(lines.size());
        for (var implName : lines) {
            LOG.trace("Retrieve ModuleInfo({}, {})", implName, bundle);
            final T service;
            try {
                service = loadImpl(serviceClass, bundle, implName);
            } catch (ScanningException e) {
                LOG.warn("Failed to acquire {} from bundle {}, ignoring it", implName, bundle, e);
                continue;
            }

            services.add(service);
        }
        return services;
    }

    private static <T> @NonNull T loadImpl(final Class<T> type, final Bundle bundle, final String className)
            throws ScanningException {
        final Class<?> loadedClass;
        try {
            loadedClass = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ScanningException(e, "Failed to load class %s", className);
        }

        final Class<? extends T> providerClass;
        try {
            providerClass = loadedClass.asSubclass(type);
        } catch (ClassCastException e) {
            throw new ScanningException(e, "Failed to validate %s", loadedClass);
        }

        final Constructor<? extends T> ctor;
        try {
            ctor = providerClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ScanningException(e, "%s does not have a no-argument constructor", providerClass);
        } catch (SecurityException e) {
            throw new ScanningException(e, "Failed to reflect on %s", providerClass);
        }

        try {
            return ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ScanningException(e, "Failed to instantiate %s", providerClass);
        }
    }

    @NonNullByDefault
    private static final class ScanningException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        ScanningException(final Exception cause, final String format, final Object... args) {
            super(String.format(format, args), cause);
        }
    }
}
