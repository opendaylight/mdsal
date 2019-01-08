/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.blueprint;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.services.BlueprintExtenderService;
import org.apache.aries.quiesce.participant.QuiesceParticipant;
import org.apache.aries.util.AriesFrameworkUtil;
import org.opendaylight.controller.blueprint.ext.OpendaylightNamespaceHandler;
import org.opendaylight.mdsal.blueprint.restart.api.BlueprintContainerRestartService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
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
public class BlueprintBundleTracker implements BundleActivator, BundleTrackerCustomizer<Bundle>, BlueprintListener {
    private static final Logger LOG = LoggerFactory.getLogger(BlueprintBundleTracker.class);
    private static final String BLUEPRINT_FILE_PATH = "org/opendaylight/blueprint/";
    private static final String BLUEPRINT_FLE_PATTERN = "*.xml";

    private ServiceTracker<BlueprintExtenderService, BlueprintExtenderService> blueprintExtenderServiceTracker;
    private ServiceTracker<QuiesceParticipant, QuiesceParticipant> quiesceParticipantTracker;
    private BundleTracker<Bundle> bundleTracker;
    private BundleContext bundleContext;
    private volatile BlueprintExtenderService blueprintExtenderService;
    private volatile QuiesceParticipant quiesceParticipant;
    private volatile ServiceRegistration<?> blueprintContainerRestartReg;
    private volatile BlueprintContainerRestartServiceImpl restartService;
    private volatile boolean shuttingDown;
    private ServiceRegistration<?> eventHandlerReg;
    private ServiceRegistration<?> namespaceReg;

    /**
     * Implemented from BundleActivator.
     */
    @Override
    public void start(final BundleContext context) {
        LOG.info("Starting {}", getClass().getSimpleName());

        restartService = new BlueprintContainerRestartServiceImpl();

        bundleContext = context;

        registerBlueprintEventHandler(context);

        registerNamespaceHandler(context);

        bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE, this);

        blueprintExtenderServiceTracker = new ServiceTracker<>(context, BlueprintExtenderService.class.getName(),
                new ServiceTrackerCustomizer<BlueprintExtenderService, BlueprintExtenderService>() {
                    @Override
                    public BlueprintExtenderService addingService(
                            final ServiceReference<BlueprintExtenderService> reference) {
                        return onBlueprintExtenderServiceAdded(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<BlueprintExtenderService> reference,
                            final BlueprintExtenderService service) {
                    }

                    @Override
                    public void removedService(final ServiceReference<BlueprintExtenderService> reference,
                            final BlueprintExtenderService service) {
                    }
                });
        blueprintExtenderServiceTracker.open();

        quiesceParticipantTracker = new ServiceTracker<>(context, QuiesceParticipant.class.getName(),
                new ServiceTrackerCustomizer<QuiesceParticipant, QuiesceParticipant>() {
                    @Override
                    public QuiesceParticipant addingService(
                            final ServiceReference<QuiesceParticipant> reference) {
                        return onQuiesceParticipantAdded(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<QuiesceParticipant> reference,
                                                final QuiesceParticipant service) {
                    }

                    @Override
                    public void removedService(final ServiceReference<QuiesceParticipant> reference,
                                               final QuiesceParticipant service) {
                    }
                });
        quiesceParticipantTracker.open();
    }

    private QuiesceParticipant onQuiesceParticipantAdded(final ServiceReference<QuiesceParticipant> reference) {
        quiesceParticipant = reference.getBundle().getBundleContext().getService(reference);

        LOG.debug("Got QuiesceParticipant");

        restartService.setQuiesceParticipant(quiesceParticipant);

        return quiesceParticipant;
    }

    private BlueprintExtenderService onBlueprintExtenderServiceAdded(
            final ServiceReference<BlueprintExtenderService> reference) {
        blueprintExtenderService = reference.getBundle().getBundleContext().getService(reference);
        bundleTracker.open();

        bundleContext.addBundleListener(BlueprintBundleTracker.this);

        LOG.debug("Got BlueprintExtenderService");

        restartService.setBlueprintExtenderService(blueprintExtenderService);

        blueprintContainerRestartReg = bundleContext.registerService(
                BlueprintContainerRestartService.class, restartService, new Hashtable<>());

        return blueprintExtenderService;
    }

    private void registerNamespaceHandler(final BundleContext context) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.service.blueprint.namespace", OpendaylightNamespaceHandler.NAMESPACE_1_0_0);
        namespaceReg = context.registerService(NamespaceHandler.class, new OpendaylightNamespaceHandler(), props);
    }

    private void registerBlueprintEventHandler(final BundleContext context) {
        eventHandlerReg = context.registerService(BlueprintListener.class, this, new Hashtable<>());
    }

    /**
     * Implemented from BundleActivator.
     */
    @Override
    public void stop(final BundleContext context) {
        bundleTracker.close();
        blueprintExtenderServiceTracker.close();
        quiesceParticipantTracker.close();

        AriesFrameworkUtil.safeUnregisterService(eventHandlerReg);
        AriesFrameworkUtil.safeUnregisterService(namespaceReg);
        AriesFrameworkUtil.safeUnregisterService(blueprintContainerRestartReg);
    }

    /**
     * Implemented from BundleActivator.
     */
    @Override
    public Bundle addingBundle(final Bundle bundle, final BundleEvent event) {
        modifiedBundle(bundle, event, bundle);
        return bundle;
    }

    /**
     * Implemented from BundleTrackerCustomizer.
     */
    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Bundle object) {
        if (shuttingDown) {
            return;
        }

        if (bundle.getState() == Bundle.ACTIVE) {
            List<Object> paths = findBlueprintPaths(bundle);

            if (!paths.isEmpty()) {
                LOG.info("Creating blueprint container for bundle {} with paths {}", bundle, paths);

                blueprintExtenderService.createContainer(bundle, paths);
            }
        }
    }

    /**
     * Implemented from BundleTrackerCustomizer.
     */
    @Override
    public void removedBundle(final Bundle bundle, final BundleEvent event, final Bundle object) {
        // BlueprintExtenderService will handle this.
    }

    /**
     * Implemented from BlueprintListener to listen for blueprint events.
     *
     * @param event the event to handle
     */
    @Override
    public void blueprintEvent(BlueprintEvent event) {
        if (event.getType() == BlueprintEvent.CREATED) {
            LOG.info("Blueprint container for bundle {} was successfully created", event.getBundle());
            return;
        }

        // If the container timed out waiting for dependencies, we'll destroy it and start it again. This
        // is indicated via a non-null DEPENDENCIES property containing the missing dependencies. The
        // default timeout is 5 min and ideally we would set this to infinite but the timeout can only
        // be set at the bundle level in the manifest - there's no way to set it globally.
        if (event.getType() == BlueprintEvent.FAILURE && event.getDependencies() != null) {
            Bundle bundle = event.getBundle();

            List<Object> paths = findBlueprintPaths(bundle);
            if (!paths.isEmpty()) {
                LOG.warn("Blueprint container for bundle {} timed out waiting for dependencies - restarting it",
                        bundle);

                restartService.restartContainer(bundle, paths);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static List<Object> findBlueprintPaths(final Bundle bundle) {
        Enumeration<?> rntries = bundle.findEntries(BLUEPRINT_FILE_PATH, BLUEPRINT_FLE_PATTERN, false);
        if (rntries == null) {
            return Collections.emptyList();
        } else {
            return Collections.list((Enumeration)rntries);
        }
    }
}
