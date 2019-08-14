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
import org.opendaylight.mdsal.binding.dom.codec.osgi.BindingRuntimeContextService;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public final class Activator implements BundleActivator {
    private final List<ServiceRegistration<?>> registrations = new ArrayList<>(2);

    private ModuleInfoBundleTracker bundleTracker = null;
    private SimpleBindingRuntimeContextService service = null;

    @Override
    public void start(final BundleContext context) {
        // XXX: this will use thread-context class loader, which is probably appropriate
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();

        service = new SimpleBindingRuntimeContextService(context, moduleInfoBackedContext, moduleInfoBackedContext);

        final OsgiModuleInfoRegistry registry = new OsgiModuleInfoRegistry(moduleInfoBackedContext,
                moduleInfoBackedContext, service);

        bundleTracker = new ModuleInfoBundleTracker(context, registry);
        bundleTracker.open();

        service.open();
        registrations.add(context.registerService(BindingRuntimeContextService.class, service, null));
        registrations.add(context.registerService(ClassLoadingStrategy.class, moduleInfoBackedContext, null));
    }

    @Override
    public void stop(final BundleContext context) {
        bundleTracker.close();
        service.close();
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }
}
