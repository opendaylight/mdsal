/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.osgi.impl;

import java.util.Collection;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;

public final class Activator implements BundleActivator {
    private BundleTracker<Collection<ObjectRegistration<YangModuleInfo>>> moduleInfoResolvedBundleTracker;
    private OsgiModuleInfoRegistry registry;
    private ServiceRegistration<?> clsReg;

    @Override
    public void start(final BundleContext context) throws Exception {
        // XXX: this will use thread-context class loader, which is probably appropriate
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();

        registry = new OsgiModuleInfoRegistry(moduleInfoBackedContext, moduleInfoBackedContext,
            moduleInfoBackedContext, moduleInfoBackedContext);

        final ModuleInfoBundleTracker moduleInfoTracker = new ModuleInfoBundleTracker(registry);
        moduleInfoResolvedBundleTracker = new BundleTracker<>(context, Bundle.RESOLVED | Bundle.STARTING
                | Bundle.STOPPING | Bundle.ACTIVE, moduleInfoTracker);
        moduleInfoResolvedBundleTracker.open();
        moduleInfoTracker.finishStart();

        registry.open(context);
        clsReg = context.registerService(ClassLoadingStrategy.class, moduleInfoBackedContext, null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        moduleInfoResolvedBundleTracker.close();
        registry.close();
        clsReg.unregister();
    }
}
