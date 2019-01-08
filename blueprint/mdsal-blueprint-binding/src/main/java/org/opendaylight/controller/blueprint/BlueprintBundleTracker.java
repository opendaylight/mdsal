/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.blueprint;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.util.AriesFrameworkUtil;
import org.opendaylight.controller.blueprint.ext.OpendaylightNamespaceHandler;
import org.opendaylight.mdsal.blueprint.restart.api.BlueprintContainerRestartService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is created in bundle activation and scans ACTIVE bundles for blueprint XML files located under
 * the well-known org/opendaylight/blueprint/ path and deploys the XML files via the Aries
 * BlueprintExtenderService. This path differs from the standard OSGI-INF/blueprint path to allow for
 * controlled deployment of blueprint containers in an orderly manner.
 *
 * @author Thomas Pantelis
 */
public class BlueprintBundleTracker implements BundleActivator {
    private static final Logger LOG = LoggerFactory.getLogger(BlueprintBundleTracker.class);

    private ServiceTracker<BlueprintContainerRestartService, BlueprintContainerRestartService> restartServiceTracker;
    private ServiceRegistration<?> namespaceReg;
    private BundleContext bundleContext;

    /**
     * Implemented from BundleActivator.
     */
    @Override
    public void start(final BundleContext context) {
        LOG.info("Starting {}", getClass().getSimpleName());

        bundleContext = context;

        restartServiceTracker = new ServiceTracker<>(context, BlueprintContainerRestartService.class,
                new ServiceTrackerCustomizer<BlueprintContainerRestartService, BlueprintContainerRestartService>() {
                    @Override
                    public BlueprintContainerRestartService addingService(
                            final ServiceReference<BlueprintContainerRestartService> reference) {
                        return onRestartServiceAdded(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<BlueprintContainerRestartService> reference,
                            final BlueprintContainerRestartService service) {
                    }

                    @Override
                    public void removedService(final ServiceReference<BlueprintContainerRestartService> reference,
                            final BlueprintContainerRestartService service) {
                    }
                });
        restartServiceTracker.open();
    }

    private BlueprintContainerRestartService onRestartServiceAdded(
            final ServiceReference<BlueprintContainerRestartService> reference) {
        final BlueprintContainerRestartService restartService = reference.getBundle().getBundleContext()
                .getService(reference);
        LOG.debug("Got BlueprintContainerRestartService");

        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.service.blueprint.namespace", OpendaylightNamespaceHandler.NAMESPACE_1_0_0);
        namespaceReg = bundleContext.registerService(NamespaceHandler.class,
            new OpendaylightNamespaceHandler(restartService), props);

        return restartService;
    }

    /**
     * Implemented from BundleActivator.
     */
    @Override
    public void stop(final BundleContext context) {
        restartServiceTracker.close();
        AriesFrameworkUtil.safeUnregisterService(namespaceReg);
    }
}
