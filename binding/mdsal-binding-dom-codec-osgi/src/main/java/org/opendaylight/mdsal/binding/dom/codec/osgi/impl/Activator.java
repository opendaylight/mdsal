/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.ModuleInfoBackedContext;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.opendaylight.yangtools.yang.xpath.impl.AntlrXPathParserFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Activator implements BundleActivator {
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>(2);

    private ModuleInfoBundleTracker bundleTracker = null;

    @Override
    public void start(final BundleContext context) {
        LOG.info("Binding-DOM codec starting");

        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create(
            "binding-dom-codec",
            new YangParserFactoryImpl(DefaultReactors.defaultReactorBuilder(new AntlrXPathParserFactory()).build()),
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());

        final OsgiModuleInfoRegistry registry = new OsgiModuleInfoRegistry(moduleInfoBackedContext,
                moduleInfoBackedContext);

        LOG.debug("Starting Binding-DOM codec bundle tracker");
        bundleTracker = new ModuleInfoBundleTracker(context, registry);
        bundleTracker.open();

        LOG.debug("Registering Binding-DOM codec services");
        registrations.add(context.registerService(ClassLoadingStrategy.class, moduleInfoBackedContext, null));

        LOG.info("Binding-DOM codec started");
    }

    @Override
    public void stop(final BundleContext context) {
        LOG.info("Binding-DOM codec stopping");

        LOG.debug("Unregistering Binding-DOM codec services");
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();

        LOG.debug("Stopping Binding-DOM codec bundle tracker");
        bundleTracker.close();

        LOG.info("Binding-DOM codec stopped");
    }
}
