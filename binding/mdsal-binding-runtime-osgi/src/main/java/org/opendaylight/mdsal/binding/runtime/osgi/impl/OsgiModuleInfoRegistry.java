/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.ModuleInfoBackedContext;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class OsgiModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiModuleInfoRegistry.class);

    private final BundleContext bundleContext;
    private final BindingRuntimeGenerator generator;
    private final ModuleInfoBackedContext moduleInfoRegistry;

    @GuardedBy("this")
    private ServiceRegistration<?> registration;

    OsgiModuleInfoRegistry(final BundleContext bundleContext, final YangParserFactory factory,
            final BindingRuntimeGenerator generator) {
        this.bundleContext = requireNonNull(bundleContext);
        this.generator = requireNonNull(generator);

        moduleInfoRegistry = ModuleInfoBackedContext.create("binding-dom-codec", factory,
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    synchronized void updateService() {
        final SchemaContext context;
        try {
            context = moduleInfoRegistry.getSchemaContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.error("Error updating the schema context", e);
            return;
        }
        LOG.trace("Assembled context {}", context);

        final BindingRuntimeContext next = DefaultBindingRuntimeContext.create(generator.generateTypeMapping(context),
            // FIXME: MDSAL-392: UGH, this should be a snapshot
            moduleInfoRegistry);

        final ServiceRegistration<?> reg = bundleContext.registerService(BindingRuntimeContext.class, next, null);
        if (registration != null) {
            registration.unregister();
        }
        registration = reg;
    }

    synchronized void close() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    ObjectRegistration<YangModuleInfo> registerInfo(final YangModuleInfo yangModuleInfo) {
        return moduleInfoRegistry.registerModuleInfo(yangModuleInfo);
    }
}
