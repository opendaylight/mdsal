/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class DOMClusterSingletonServiceProviderImpl
        implements ClusterSingletonServiceProvider, DOMEntityOwnershipListener {

    private static final Logger LOG = LoggerFactory.getLogger(DOMClusterSingletonServiceProviderImpl.class);

    @VisibleForTesting
    static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    @VisibleForTesting
    static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final DOMEntityOwnershipService entityOwnershipService;
    private final Map<String, ClusterSingletonServiceGroup> serviceGroupMap = new ConcurrentHashMap<>();

    /* EOS Entity Listeners Registration */
    private DOMEntityOwnershipListenerRegistration serviceEntityListenerReg;
    private DOMEntityOwnershipListenerRegistration asyncCloseEntityListenerReg;

    /**
     * Class constructor.
     *
     * @param entityOwnershipService relevant EOS
     */
    @Inject
    public DOMClusterSingletonServiceProviderImpl(final @NonNull DOMEntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = requireNonNull(entityOwnershipService);
    }

    /**
     * This method must be called once on startup to initialize this provider.
     */
    public void initializeProvider() {
        LOG.debug("Initialization method for ClusterSingletonService Provider {}", this);
        this.serviceEntityListenerReg = entityOwnershipService.registerListener(SERVICE_ENTITY_TYPE, this);
        this.asyncCloseEntityListenerReg = entityOwnershipService.registerListener(CLOSE_SERVICE_ENTITY_TYPE, this);
    }

    @Override
    public synchronized ClusterSingletonServiceRegistration registerClusterSingletonService(
            final ClusterSingletonService service) {
        LOG.debug("Call registrationService {} method for ClusterSingletonService Provider {}", service, this);

        final String serviceIdentifier = service.getIdentifier().getName();
        checkArgument(!Strings.isNullOrEmpty(serviceIdentifier),
            "ClusterSingletonService identifier may not be null nor empty");

        final ClusterSingletonServiceGroup serviceGroup;
        ClusterSingletonServiceGroup existing = serviceGroupMap.get(serviceIdentifier);
        if (existing == null) {
            serviceGroup = createGroup(serviceIdentifier, new ArrayList<>(1));
            serviceGroupMap.put(serviceIdentifier, serviceGroup);

            try {
                initializeOrRemoveGroup(serviceGroup);
            } catch (CandidateAlreadyRegisteredException e) {
                throw new IllegalArgumentException("Service group already registered", e);
            }
        } else {
            serviceGroup = existing;
        }

        final ClusterSingletonServiceRegistration reg =  new AbstractClusterSingletonServiceRegistration(service) {
            @Override
            protected void removeRegistration() {
                // We need to bounce the unregistration through a ordered lock in order not to deal with asynchronous
                // shutdown of the group and user registering it again.
                DOMClusterSingletonServiceProviderImpl.this.removeRegistration(serviceIdentifier, this);
            }
        };

        serviceGroup.registerService(reg);
        return reg;
    }

    @Override
    public void ownershipChanged(final DOMEntityOwnershipChange ownershipChange) {
        LOG.debug("Ownership change for ClusterSingletonService Provider {}", ownershipChange);
        final String serviceIdentifier = getServiceIdentifierFromEntity(ownershipChange.getEntity());
        final ClusterSingletonServiceGroup serviceHolder = serviceGroupMap.get(serviceIdentifier);
        if (serviceHolder != null) {
            serviceHolder.ownershipChanged(ownershipChange);
        } else {
            LOG.debug("ClusterSingletonServiceGroup was not found for serviceIdentifier {}", serviceIdentifier);
        }
    }

    @Override
    public void close() {
        LOG.debug("Close method for ClusterSingletonService Provider {}", this);

        if (serviceEntityListenerReg != null) {
            serviceEntityListenerReg.close();
            serviceEntityListenerReg = null;
        }

        final List<ListenableFuture<?>> listGroupCloseListFuture = new ArrayList<>();
        for (final ClusterSingletonServiceGroup serviceGroup : serviceGroupMap.values()) {
            listGroupCloseListFuture.add(serviceGroup.closeClusterSingletonGroup());
        }

        final ListenableFuture<List<Object>> finalCloseFuture = Futures.allAsList(listGroupCloseListFuture);
        Futures.addCallback(finalCloseFuture, new FutureCallback<List<?>>() {

            @Override
            public void onSuccess(final List<?> result) {
                cleanup();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Unexpected problem by closing ClusterSingletonServiceProvider {}",
                    DOMClusterSingletonServiceProviderImpl.this, throwable);
                cleanup();
            }
        }, MoreExecutors.directExecutor());
    }

    private ClusterSingletonServiceGroup createGroup(final String serviceIdentifier,
            final List<ClusterSingletonServiceRegistration> services) {
        return new ClusterSingletonServiceGroupImpl(serviceIdentifier, entityOwnershipService,
                new DOMEntity(SERVICE_ENTITY_TYPE, serviceIdentifier),
                new DOMEntity(CLOSE_SERVICE_ENTITY_TYPE, serviceIdentifier), services);
    }

    private void initializeOrRemoveGroup(final ClusterSingletonServiceGroup group)
            throws CandidateAlreadyRegisteredException {
        try {
            group.initialize();
        } catch (CandidateAlreadyRegisteredException e) {
            serviceGroupMap.remove(group.getIdentifier(), group);
            throw e;
        }
    }

    void removeRegistration(final String serviceIdentifier, final ClusterSingletonServiceRegistration reg) {
        final PlaceholderGroup placeHolder;
        final ListenableFuture<?> future;
        synchronized (this) {
            final ClusterSingletonServiceGroup lookup = verifyNotNull(serviceGroupMap.get(serviceIdentifier));
            future = lookup.unregisterService(reg);
            if (future == null) {
                return;
            }

            // Close the group and replace it with a placeholder
            LOG.debug("Closing service group {}", serviceIdentifier);
            placeHolder = new PlaceholderGroup(lookup, future);

            final String identifier = reg.getInstance().getIdentifier().getName();
            verify(serviceGroupMap.replace(identifier, lookup, placeHolder));
            LOG.debug("Replaced group {} with {}", serviceIdentifier, placeHolder);

            lookup.closeClusterSingletonGroup();
        }

        future.addListener(() -> finishShutdown(placeHolder), MoreExecutors.directExecutor());
    }

    synchronized void finishShutdown(final PlaceholderGroup placeHolder) {
        final String identifier = placeHolder.getIdentifier();
        LOG.debug("Service group {} closed", identifier);

        final List<ClusterSingletonServiceRegistration> services = placeHolder.getServices();
        if (services.isEmpty()) {
            // No services, we are all done
            if (serviceGroupMap.remove(identifier, placeHolder)) {
                LOG.debug("Service group {} removed", placeHolder);
            } else {
                LOG.debug("Service group {} superseded by {}", placeHolder, serviceGroupMap.get(identifier));
            }
            return;
        }

        // Placeholder is being retired, we are reusing its services as the seed for the group.
        final ClusterSingletonServiceGroup group = createGroup(identifier, services);
        verify(serviceGroupMap.replace(identifier, placeHolder, group));
        placeHolder.setSuccessor(group);
        LOG.debug("Service group upgraded from {} to {}", placeHolder, group);

        try {
            initializeOrRemoveGroup(group);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Failed to register delayed group {}, it will remain inoperational", identifier, e);
        }
    }

    /**
     * Method is called async. from close method in end of Provider lifecycle.
     */
    void cleanup() {
        LOG.debug("Final cleaning ClusterSingletonServiceProvider {}", this);
        if (asyncCloseEntityListenerReg != null) {
            asyncCloseEntityListenerReg.close();
            asyncCloseEntityListenerReg = null;
        }
        serviceGroupMap.clear();
    }

    private static String getServiceIdentifierFromEntity(final DOMEntity entity) {
        return ((NodeIdentifierWithPredicates) entity.getIdentifier().getLastPathArgument()).values().iterator().next()
                .toString();
    }
}
