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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class {@link AbstractClusterSingletonServiceProviderImpl} represents implementations of
 * {@link ClusterSingletonServiceProvider} and it implements {@link GenericEntityOwnershipListener} for providing
 * OwnershipChange for all registered {@link ClusterSingletonServiceGroup} entity candidate.
 */
public abstract class AbstractClusterSingletonServiceProviderImpl
        implements ClusterSingletonServiceProvider, DOMEntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractClusterSingletonServiceProviderImpl.class);

    @VisibleForTesting
    static final @NonNull String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    @VisibleForTesting
    static final @NonNull String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final ConcurrentMap<String, ClusterSingletonServiceGroup> serviceGroupMap = new ConcurrentHashMap<>();
    private final DOMEntityOwnershipService entityOwnershipService;

    /* EOS Entity Listeners Registration */
    private Registration serviceEntityListenerReg;
    private Registration asyncCloseEntityListenerReg;

    /**
     * Class constructor.
     *
     * @param entityOwnershipService relevant EOS
     */
    protected AbstractClusterSingletonServiceProviderImpl(
            final @NonNull DOMEntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = requireNonNull(entityOwnershipService);
    }

    /**
     * This method must be called once on startup to initialize this provider.
     */
    public final void initializeProvider() {
        LOG.debug("Initialization method for ClusterSingletonService Provider {}", this);
        serviceEntityListenerReg = registerListener(SERVICE_ENTITY_TYPE, entityOwnershipService);
        asyncCloseEntityListenerReg = registerListener(CLOSE_SERVICE_ENTITY_TYPE, entityOwnershipService);
    }

    @Override
    public final synchronized ClusterSingletonServiceRegistration registerClusterSingletonService(
            final ClusterSingletonService service) {
        LOG.debug("Call registrationService {} method for ClusterSingletonService Provider {}", service, this);

        final String serviceIdentifier = service.getIdentifier().getName();
        checkArgument(!Strings.isNullOrEmpty(serviceIdentifier),
            "ClusterSingletonService identifier may not be null nor empty");

        final ClusterSingletonServiceGroup serviceGroup;
        final var existing = serviceGroupMap.get(serviceIdentifier);
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

        final var reg =  new AbstractClusterSingletonServiceRegistration(service) {
            @Override
            protected void removeRegistration() {
                // We need to bounce the unregistration through a ordered lock in order not to deal with asynchronous
                // shutdown of the group and user registering it again.
                AbstractClusterSingletonServiceProviderImpl.this.removeRegistration(serviceIdentifier, this);
            }
        };

        serviceGroup.registerService(reg);
        return reg;
    }

    private ClusterSingletonServiceGroup createGroup(final String serviceIdentifier,
            final List<ClusterSingletonServiceRegistration> services) {
        return new ClusterSingletonServiceGroupImpl(serviceIdentifier, entityOwnershipService,
            createEntity(SERVICE_ENTITY_TYPE, serviceIdentifier),
            createEntity(CLOSE_SERVICE_ENTITY_TYPE, serviceIdentifier), services);
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
            final var lookup = verifyNotNull(serviceGroupMap.get(serviceIdentifier));
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
        final var identifier = placeHolder.getIdentifier();
        LOG.debug("Service group {} closed", identifier);

        final var services = placeHolder.getServices();
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
        final var group = createGroup(identifier, services);
        verify(serviceGroupMap.replace(identifier, placeHolder, group));
        placeHolder.setSuccessor(group);
        LOG.debug("Service group upgraded from {} to {}", placeHolder, group);

        try {
            initializeOrRemoveGroup(group);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Failed to register delayed group {}, it will remain inoperational", identifier, e);
        }
    }

    @Override
    public final void close() {
        LOG.debug("Close method for ClusterSingletonService Provider {}", this);

        if (serviceEntityListenerReg != null) {
            serviceEntityListenerReg.close();
            serviceEntityListenerReg = null;
        }

        final var futures = serviceGroupMap.values().stream()
            .map(ClusterSingletonServiceGroup::closeClusterSingletonGroup)
            .toList();
        final var future = Futures.allAsList(futures);
        Futures.addCallback(future, new FutureCallback<>() {
            @Override
            public void onSuccess(final List<Object> result) {
                cleanup();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Unexpected problem by closing ClusterSingletonServiceProvider {}",
                    AbstractClusterSingletonServiceProviderImpl.this, throwable);
                cleanup();
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    public final void ownershipChanged(final DOMEntity entity, final EntityOwnershipStateChange change,
            final boolean inJeopardy) {
        LOG.debug("Ownership change for ClusterSingletonService Provider on {} {} inJeopardy={}", entity, change,
            inJeopardy);
        final var serviceIdentifier = getServiceIdentifierFromEntity(entity);
        final var serviceHolder = serviceGroupMap.get(serviceIdentifier);
        if (serviceHolder != null) {
            serviceHolder.ownershipChanged(entity, change, inJeopardy);
        } else {
            LOG.debug("ClusterSingletonServiceGroup was not found for serviceIdentifier {}", serviceIdentifier);
        }
    }

    /**
     * Method implementation registers the listener.
     *
     * @param entityType the type of the entity
     * @param eos - EOS type
     * @return a {@link Registration}
     */
    protected abstract Registration registerListener(String entityType, DOMEntityOwnershipService eos);

    /**
     * Creates an extended {@link GenericEntity} instance.
     *
     * @param entityType the type of the entity
     * @param entityIdentifier the identifier of the entity
     * @return instance of Entity extended GenericEntity type
     */
    @VisibleForTesting
    static final DOMEntity createEntity(final String entityType, final String entityIdentifier) {
        return new DOMEntity(entityType, entityIdentifier);
    }

    /**
     * Method is responsible for parsing ServiceGroupIdentifier from E entity.
     *
     * @param entity instance of GenericEntity type
     * @return ServiceGroupIdentifier parsed from entity key value.
     */
    protected abstract String getServiceIdentifierFromEntity(DOMEntity entity);

    /**
     * Method is called async. from close method in end of Provider lifecycle.
     */
    final void cleanup() {
        LOG.debug("Final cleaning ClusterSingletonServiceProvider {}", this);
        if (asyncCloseEntityListenerReg != null) {
            asyncCloseEntityListenerReg.close();
            asyncCloseEntityListenerReg = null;
        }
        serviceGroupMap.clear();
    }
}
