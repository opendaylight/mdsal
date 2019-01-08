/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.blueprint.restart.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.aries.blueprint.services.BlueprintExtenderService;
import org.apache.aries.quiesce.participant.QuiesceParticipant;
import org.apache.aries.util.AriesFrameworkUtil;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.blueprint.restart.api.BlueprintContainerRestartService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the BlueprintContainerRestartService.
 *
 * @author Thomas Pantelis
 */
public class BlueprintContainerRestartServiceImpl implements AutoCloseable, BlueprintContainerRestartService,
        SynchronousBundleListener {
    private static final Logger LOG = LoggerFactory.getLogger(BlueprintContainerRestartServiceImpl.class);
    private static final int CONTAINER_CREATE_TIMEOUT_IN_MINUTES = 5;
    private static final long SYSTEM_BUNDLE_ID = 0;

    private final ExecutorService restartExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true).setNameFormat("BlueprintContainerRestartService").build());

    private final BlueprintExtenderService blueprintExtenderService;
    private final QuiesceParticipant quiesceParticipant;
    private final BundleContext bundleContext;

    public BlueprintContainerRestartServiceImpl(final BundleContext bundleContext,
            final BlueprintExtenderService blueprintExtenderService, final QuiesceParticipant quiesceParticipant) {
        this.bundleContext = requireNonNull(bundleContext);
        this.blueprintExtenderService = requireNonNull(blueprintExtenderService);
        this.quiesceParticipant = requireNonNull(quiesceParticipant);
    }

    @Override
    public void restartContainer(final Bundle bundle) {
        LOG.debug("restartContainer for bundle {}", bundle);

        if (restartExecutor.isShutdown()) {
            LOG.debug("Already closed - returning");
            return;
        }

        restartExecutor.execute(() -> {
            blueprintExtenderService.destroyContainer(bundle, blueprintExtenderService.getContainer(bundle));
            blueprintExtenderService.createContainer(bundle);
        });
    }

    @Override
    public void restartContainerAndDependents(final Bundle bundle) {
        if (restartExecutor.isShutdown()) {
            return;
        }

        LOG.debug("restartContainerAndDependents for bundle {}", bundle);

        restartExecutor.execute(() -> restartContainerAndDependentsInternal(bundle));
    }

    /**
     * Implemented from SynchronousBundleListener.
     */
    @Override
    public void bundleChanged(final BundleEvent event) {
        // If the system bundle (id 0) is stopping, do an orderly shutdown of all blueprint containers. On
        // shutdown the system bundle is stopped first.
        if (event.getBundle().getBundleId() == SYSTEM_BUNDLE_ID && event.getType() == BundleEvent.STOPPING) {
            shutdownAllContainers();
        }
    }

    private void restartContainerAndDependentsInternal(final Bundle forBundle) {
        // We use a LinkedHashSet to preserve insertion order as we walk the service usage hierarchy.
        Set<Bundle> containerBundlesSet = new LinkedHashSet<>();
        findDependentContainersRecursively(forBundle, containerBundlesSet);

        List<Bundle> containerBundles = new ArrayList<>(containerBundlesSet);

        LOG.info("Restarting blueprint containers for bundle {} and its dependent bundles {}", forBundle,
                containerBundles.subList(1, containerBundles.size()));

        // The blueprint containers are created asynchronously so we register a handler for blueprint events
        // that are sent when a container is complete, successful or not. The CountDownLatch tells when all
        // containers are complete. This is done to ensure all blueprint containers are finished before we
        // restart config modules.
        final CountDownLatch containerCreationComplete = new CountDownLatch(containerBundles.size());
        ServiceRegistration<?> eventHandlerReg = registerEventHandler(forBundle.getBundleContext(), event -> {
            final Bundle bundle = event.getBundle();
            if (event.isReplay()) {
                LOG.trace("Got replay BlueprintEvent {} for bundle {}", event.getType(), bundle);
                return;
            }

            LOG.debug("Got BlueprintEvent {} for bundle {}", event.getType(), bundle);
            if (containerBundles.contains(bundle)
                    && (event.getType() == BlueprintEvent.CREATED || event.getType() == BlueprintEvent.FAILURE)) {
                containerCreationComplete.countDown();
                LOG.debug("containerCreationComplete is now {}", containerCreationComplete.getCount());
            }
        });

        final Runnable createContainerCallback = () -> createContainers(containerBundles);

        // Destroy the container down-top recursively and once done, restart the container top-down
        destroyContainers(new ArrayDeque<>(Lists.reverse(containerBundles)), createContainerCallback);


        try {
            if (!containerCreationComplete.await(CONTAINER_CREATE_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES)) {
                LOG.warn("Failed to restart all blueprint containers within {} minutes. Attempted to restart {} {} "
                        + "but only {} completed restart", CONTAINER_CREATE_TIMEOUT_IN_MINUTES, containerBundles.size(),
                        containerBundles, containerBundles.size() - containerCreationComplete.getCount());
                return;
            }
        } catch (final InterruptedException e) {
            LOG.debug("CountDownLatch await was interrupted - returning");
            return;
        }

        AriesFrameworkUtil.safeUnregisterService(eventHandlerReg);

        LOG.info("Finished restarting blueprint containers for bundle {} and its dependent bundles", forBundle);
    }

    /**
     * Recursively quiesce and destroy the bundles one by one in order to maintain synchronicity and ordering.
     * @param remainingBundlesToDestroy the list of remaining bundles to destroy.
     * @param createContainerCallback a {@link Runnable} to {@code run()} when the recursive function is completed.
     */
    private void destroyContainers(final Deque<Bundle> remainingBundlesToDestroy,
            final Runnable createContainerCallback) {

        final Bundle nextBundle;
        synchronized (remainingBundlesToDestroy) {
            if (remainingBundlesToDestroy.isEmpty()) {
                LOG.debug("All blueprint containers were quiesced and destroyed");
                createContainerCallback.run();
                return;
            }

            nextBundle = remainingBundlesToDestroy.poll();
        }

        // The Quiesce capability is a like a soft-stop, clean-stop. In the case of the Blueprint extender, in flight
        // service calls are allowed to finish; they're counted in and counted out, and no new calls are allowed. When
        // there are no in flight service calls, the bundle is told to stop. The Blueprint bundle itself doesn't know
        // this is happening which is a key design point. In the case of Blueprint, the extender ensures no new Entity
        // Managers(EMs) are created. Then when all those EMs are closed the quiesce operation reports that it is
        // finished.
        // To properly restart the blueprint containers, first we have to quiesce the list of bundles, and once done, it
        // is safe to destroy their BlueprintContainer, so no reference is retained.
        //
        // Mail - thread explaining Quiesce API:
        //      https://www.mail-archive.com/dev@aries.apache.org/msg08403.html

        // Quiesced the bundle to unregister the associated BlueprintContainer
        quiesceParticipant.quiesce(bundlesQuiesced -> {

            // Destroy the container once Quiesced
            Arrays.stream(bundlesQuiesced).forEach(quiescedBundle -> {
                LOG.debug("Quiesced bundle {}", quiescedBundle);
                blueprintExtenderService.destroyContainer(
                        quiescedBundle, blueprintExtenderService.getContainer(quiescedBundle));
            });

            destroyContainers(remainingBundlesToDestroy, createContainerCallback);

        }, Collections.singletonList(nextBundle));
    }

    private void createContainers(final List<Bundle> containerBundles) {
        containerBundles.forEach(bundle -> {
            LOG.info("Restarting blueprint container for bundle {}", bundle);
            blueprintExtenderService.createContainer(bundle);
        });
    }

    /**
     * Recursively finds the services registered by the given bundle and the bundles using those services.
     * User bundles that have an associated blueprint container are added to containerBundles.
     *
     * @param bundle the bundle to traverse
     * @param containerBundles the current set of bundles containing blueprint containers
     */
    private void findDependentContainersRecursively(final Bundle bundle, final Set<Bundle> containerBundles) {
        if (!containerBundles.add(bundle)) {
            // Already seen this bundle...
            return;
        }

        ServiceReference<?>[] references = bundle.getRegisteredServices();
        if (references != null) {
            for (ServiceReference<?> reference : references) {
                Bundle[] usingBundles = reference.getUsingBundles();
                if (usingBundles != null) {
                    for (Bundle usingBundle : usingBundles) {
                        if (blueprintExtenderService.getContainer(usingBundle) != null) {
                            findDependentContainersRecursively(usingBundle, containerBundles);
                        }
                    }
                }
            }
        }
    }

    private void shutdownAllContainers() {
        shuttingDown = true;

        LOG.info("Shutting down all blueprint containers...");

        Collection<Bundle> containerBundles = new HashSet<>(Arrays.asList(bundleContext.getBundles()));
        while (!containerBundles.isEmpty()) {
            // For each iteration of getBundlesToDestroy, as containers are destroyed, other containers become
            // eligible to be destroyed. We loop until we've destroyed them all.
            for (Bundle bundle : getBundlesToDestroy(containerBundles)) {
                containerBundles.remove(bundle);
                BlueprintContainer container = blueprintExtenderService.getContainer(bundle);
                if (container != null) {
                    blueprintExtenderService.destroyContainer(bundle, container);
                }
            }
        }

        LOG.info("Shutdown of blueprint containers complete");
    }

    private static List<Bundle> getBundlesToDestroy(final Collection<Bundle> containerBundles) {
        List<Bundle> bundlesToDestroy = new ArrayList<>();

        // Find all container bundles that either have no registered services or whose services are no
        // longer in use.
        for (Bundle bundle : containerBundles) {
            ServiceReference<?>[] references = bundle.getRegisteredServices();
            int usage = 0;
            if (references != null) {
                for (ServiceReference<?> reference : references) {
                    usage += getServiceUsage(reference);
                }
            }

            LOG.debug("Usage for bundle {} is {}", bundle, usage);
            if (usage == 0) {
                bundlesToDestroy.add(bundle);
            }
        }

        if (!bundlesToDestroy.isEmpty()) {
            bundlesToDestroy.sort((b1, b2) -> (int) (b2.getLastModified() - b1.getLastModified()));

            LOG.debug("Selected bundles {} for destroy (no services in use)", bundlesToDestroy);
        } else {
            // There's either no more container bundles or they all have services being used. For
            // the latter it means there's either circular service usage or a service is being used
            // by a non-container bundle. But we need to make progress so we pick the bundle with a
            // used service with the highest service ID. Each service is assigned a monotonically
            // increasing ID as they are registered. By picking the bundle with the highest service
            // ID, we're picking the bundle that was (likely) started after all the others and thus
            // is likely the safest to destroy at this point.

            Bundle bundle = findBundleWithHighestUsedServiceId(containerBundles);
            if (bundle != null) {
                bundlesToDestroy.add(bundle);
            }

            LOG.debug("Selected bundle {} for destroy (lowest ranking service or highest service ID)",
                    bundlesToDestroy);
        }

        return bundlesToDestroy;
    }

    private static ServiceRegistration<?> registerEventHandler(final BundleContext bundleContext,
            final BlueprintListener listener) {
        return bundleContext.registerService(BlueprintListener.class, listener, new Hashtable<>());
    }

    private static @Nullable Bundle findBundleWithHighestUsedServiceId(final Collection<Bundle> containerBundles) {
        ServiceReference<?> highestServiceRef = null;
        for (Bundle bundle : containerBundles) {
            ServiceReference<?>[] references = bundle.getRegisteredServices();
            if (references == null) {
                continue;
            }

            for (ServiceReference<?> reference : references) {
                // We did check the service usage previously but it's possible the usage has changed since then.
                if (getServiceUsage(reference) == 0) {
                    continue;
                }

                // Choose 'reference' if it has a lower service ranking or, if the rankings are equal
                // which is usually the case, if it has a higher service ID. For the latter the < 0
                // check looks backwards but that's how ServiceReference#compareTo is documented to work.
                if (highestServiceRef == null || reference.compareTo(highestServiceRef) < 0) {
                    LOG.debug("Currently selecting bundle {} for destroy (with reference {})", bundle, reference);
                    highestServiceRef = reference;
                }
            }
        }

        return highestServiceRef == null ? null : highestServiceRef.getBundle();
    }

    private static int getServiceUsage(final ServiceReference<?> ref) {
        Bundle[] usingBundles = ref.getUsingBundles();
        return usingBundles != null ? usingBundles.length : 0;
    }

    @Override
    public void close() {
        LOG.debug("Closing");

        restartExecutor.shutdownNow();
    }
}
