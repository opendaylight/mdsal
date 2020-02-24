/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public final class BindingRuntimeExtender {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeExtender.class);

    @Reference
    YangParserFactory parserFactory = null;
    @Reference
    BindingRuntimeGenerator generator = null;
    @Reference(target = "(component.factory=org.opendaylight.mdsal.binding.runtime.osgi.BindingRuntimeContextFactory)")
    ComponentFactory contextFactory = null;

    private ModuleInfoBundleTracker bundleTracker = null;
    private OsgiModuleInfoRegistry moduleRegistry = null;

    @Activate
    void activate(final BundleContext ctx) {
        LOG.info("Binding Runtime Extender starting");
        moduleRegistry = new OsgiModuleInfoRegistry(contextFactory, parserFactory, generator);
        bundleTracker = new ModuleInfoBundleTracker(ctx, moduleRegistry);
        bundleTracker.open();
        LOG.info("Binding Runtime Extender started");
    }

    @Deactivate
    void deactivate() {
        LOG.info("Binding Runtime Extender stopping");
        bundleTracker.close();
        moduleRegistry.close();
        LOG.info("Binding Runtime Extender stopped");
    }
}
