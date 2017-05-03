/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.osgi;

import java.util.Collection;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;

public final class GeneratorActivator implements BundleActivator {
    private BundleTracker<Collection<ObjectRegistration<YangModuleInfo>>> moduleInfoResolvedBundleTracker;
    private ServiceRegistration<?> clsReg;
    private ServiceRegistration<?> scpReg;

    @Override
    public void start(final BundleContext context) throws Exception {
        // the inner strategy is backed by thread context cl?
        final ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();
        scpReg = context.registerService(SchemaContextProvider.class, moduleInfoBackedContext, null);

        final SimpleBindingRuntimeContextSupplier bindingContextProvider = new SimpleBindingRuntimeContextSupplier();
        final RefreshingSCPModuleInfoRegistry registryWrapper = new RefreshingSCPModuleInfoRegistry(
                moduleInfoBackedContext, moduleInfoBackedContext, moduleInfoBackedContext, moduleInfoBackedContext,
                bindingContextProvider, scpReg);

        final ModuleInfoBundleTracker moduleInfoTracker = new ModuleInfoBundleTracker(registryWrapper);
        moduleInfoResolvedBundleTracker = new BundleTracker<>(context, Bundle.RESOLVED | Bundle.STARTING
                | Bundle.STOPPING | Bundle.ACTIVE, moduleInfoTracker);
        moduleInfoResolvedBundleTracker.open();
        moduleInfoTracker.finishStart();

        clsReg = context.registerService(ClassLoadingStrategy.class, moduleInfoBackedContext, null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        clsReg.unregister();
        scpReg.unregister();
        moduleInfoResolvedBundleTracker.close();
    }
}
