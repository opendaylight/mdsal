/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    private static final String YANG_MODLE_BINDING_PROVIDER_SERVICE = MODULE_INFO_PROVIDER_PATH_PREFIX
            + YangModelBindingProvider.class.getName();

    private final YangModuleInfoRegistry moduleInfoRegistry;

    YangModuleInfoScanner(final BundleContext context, final YangModuleInfoRegistry moduleInfoRegistry) {
        super(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE, null);
        this.moduleInfoRegistry = requireNonNull(moduleInfoRegistry);
    }

    @Override
    public List<ObjectRegistration<YangModuleInfo>> addingBundle(final Bundle bundle, final BundleEvent event) {
        if (bundle.getBundleId() == Constants.SYSTEM_BUNDLE_ID) {
            LOG.debug("Ignoring system bundle {}", bundle);
            return ImmutableList.of();
        }

        final URL resource = bundle.getEntry(YANG_MODLE_BINDING_PROVIDER_SERVICE);
        if (resource == null) {
            LOG.debug("Bundle {} does not have an entry for {}", bundle, YANG_MODLE_BINDING_PROVIDER_SERVICE);
            return ImmutableList.of();
        }

        LOG.debug("Got addingBundle({}) with YangModelBindingProvider resource {}", bundle, resource);
        final List<String> lines;
        try {
            lines = Resources.readLines(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error while reading {} from bundle {}", resource, bundle, e);
            return ImmutableList.of();
        }

        if (lines.isEmpty()) {
            LOG.debug("Bundle {} has empty services for {}", bundle, YANG_MODLE_BINDING_PROVIDER_SERVICE);
            return ImmutableList.of();
        }

        final List<YangModuleInfo> infos = new ArrayList<>(lines.size());
        for (String moduleInfoName : lines) {
            LOG.trace("Retrieve ModuleInfo({}, {})", moduleInfoName, bundle);
            final YangModuleInfo moduleInfo;
            try {
                moduleInfo = retrieveModuleInfo(moduleInfoName, bundle);
            } catch (ScanningException e) {
                LOG.warn("Failed to acquire {} from bundle {}, ignoring it", moduleInfoName, bundle, e);
                continue;
            }

            infos.add(moduleInfo);
        }

        final List<ObjectRegistration<YangModuleInfo>> registrations = moduleInfoRegistry.registerInfos(infos);
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

    private static YangModuleInfo retrieveModuleInfo(final String className, final Bundle bundle)
            throws ScanningException {
        final Class<?> loadedClass;
        try {
            loadedClass = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ScanningException(e, "Failed to load class %s", className);
        }

        final Class<? extends YangModelBindingProvider> providerClass;
        try {
            providerClass = loadedClass.asSubclass(YangModelBindingProvider.class);
        } catch (ClassCastException e) {
            throw new ScanningException(e, "Failed to validate %s", loadedClass);
        }

        final Constructor<? extends YangModelBindingProvider> ctor;
        try {
            ctor = providerClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ScanningException(e, "%s does not have a no-argument constructor", providerClass);
        } catch (SecurityException e) {
            throw new ScanningException(e, "Failed to reflect on %s", providerClass);
        }

        YangModelBindingProvider instance;
        try {
            instance = ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ScanningException(e, "Failed to instantiate %s", providerClass);
        }

        return instance.getModuleInfo();
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
