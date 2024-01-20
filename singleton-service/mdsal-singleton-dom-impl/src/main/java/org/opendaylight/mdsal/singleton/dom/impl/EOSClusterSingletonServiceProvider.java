/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ClusterSingletonServiceProvider} working on top a {@link DOMEntityOwnershipService}.
 */
@Singleton
@Component(service = ClusterSingletonServiceProvider.class)
public final class EOSClusterSingletonServiceProvider
        implements ClusterSingletonServiceProvider, DOMEntityOwnershipListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(EOSClusterSingletonServiceProvider.class);

    @VisibleForTesting
    static final @NonNull String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    @VisibleForTesting
    static final @NonNull String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final ConcurrentMap<String, ServiceGroup> serviceGroupMap = new ConcurrentHashMap<>();
    private final DOMEntityOwnershipService entityOwnershipService;

    /* EOS Entity Listeners Registration */
    private Registration serviceEntityListenerReg;
    private Registration asyncCloseEntityListenerReg;

    @Inject
    @Activate
    public EOSClusterSingletonServiceProvider(@Reference final DOMEntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = requireNonNull(entityOwnershipService);
        serviceEntityListenerReg = entityOwnershipService.registerListener(SERVICE_ENTITY_TYPE, this);
        asyncCloseEntityListenerReg = entityOwnershipService.registerListener(CLOSE_SERVICE_ENTITY_TYPE, this);
        LOG.info("Cluster Singleton Service started");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() throws ExecutionException, InterruptedException {
        final Registration reg;
        final ListenableFuture<?> future;
        synchronized (this) {
            if (serviceEntityListenerReg == null) {
                // Idempotent
                return;
            }

            LOG.info("Cluster Singleton Service stopping");
            reg = serviceEntityListenerReg;
            serviceEntityListenerReg = null;
            future = Futures.allAsList(serviceGroupMap.values().stream()
                .map(ServiceGroup::closeClusterSingletonGroup)
                .toList());
        }

        try {
            LOG.debug("Waiting for service groups to stop");
            future.get();
        } finally {
            reg.close();
            asyncCloseEntityListenerReg.close();
            asyncCloseEntityListenerReg = null;
            serviceGroupMap.clear();
            LOG.info("Cluster Singleton Service stopped");
        }
    }

    @Override
    public synchronized Registration registerClusterSingletonService(final ClusterSingletonService service) {
        final var serviceIdentifier = requireNonNull(service.getIdentifier());
        if (serviceEntityListenerReg == null) {
            throw new IllegalStateException(this + "is closed");
        }

        LOG.debug("Call registrationService {} method for ClusterSingletonService Provider {}", service, this);

        final var identifierValue = serviceIdentifier.value();
        final ServiceGroup serviceGroup;
        final var existing = serviceGroupMap.get(identifierValue);
        if (existing == null) {
            serviceGroup = createGroup(serviceIdentifier, new ArrayList<>(1));
            serviceGroupMap.put(identifierValue, serviceGroup);

            try {
                initializeOrRemoveGroup(serviceGroup);
            } catch (CandidateAlreadyRegisteredException e) {
                throw new IllegalArgumentException("Service group already registered", e);
            }
        } else {
            serviceGroup = existing;
        }

        final var reg = new ServiceRegistration(service) {
            @Override
            protected void removeRegistration() {
                // We need to bounce the unregistration through a ordered lock in order not to deal with asynchronous
                // shutdown of the group and user registering it again.
                EOSClusterSingletonServiceProvider.this.removeRegistration(serviceIdentifier, this);
            }
        };

        serviceGroup.registerService(reg);
        return reg;
    }

    private ServiceGroup createGroup(final ServiceGroupIdentifier identifier,
            final List<ServiceRegistration> services) {
        return new ActiveServiceGroup(identifier, entityOwnershipService,
            createEntity(SERVICE_ENTITY_TYPE, identifier), createEntity(CLOSE_SERVICE_ENTITY_TYPE, identifier),
            services);
    }

    @Holding("this")
    private void initializeOrRemoveGroup(final ServiceGroup group) throws CandidateAlreadyRegisteredException {
        try {
            group.initialize();
        } catch (CandidateAlreadyRegisteredException e) {
            serviceGroupMap.remove(group.getIdentifier(), group);
            throw e;
        }
    }

    private void removeRegistration(final ServiceGroupIdentifier serviceIdentifier, final ServiceRegistration reg) {
        final PlaceholderServiceGroup placeHolder;
        final ListenableFuture<?> future;
        synchronized (this) {
            final var lookup = verifyNotNull(serviceGroupMap.get(serviceIdentifier.value()));
            future = lookup.unregisterService(reg);
            if (future == null) {
                return;
            }

            // Close the group and replace it with a placeholder
            LOG.debug("Closing service group {}", serviceIdentifier);
            placeHolder = new PlaceholderServiceGroup(lookup, future);

            final var identifier = reg.getInstance().getIdentifier().value();
            verify(serviceGroupMap.replace(identifier, lookup, placeHolder));
            LOG.debug("Replaced group {} with {}", serviceIdentifier, placeHolder);

            lookup.closeClusterSingletonGroup();
        }

        future.addListener(() -> finishShutdown(placeHolder), MoreExecutors.directExecutor());
    }

    private synchronized void finishShutdown(final PlaceholderServiceGroup placeHolder) {
        final var identifier = placeHolder.getIdentifier();
        LOG.debug("Service group {} closed", identifier);

        final var services = placeHolder.getServices();
        if (services.isEmpty()) {
            // No services, we are all done
            if (serviceGroupMap.remove(identifier.value(), placeHolder)) {
                LOG.debug("Service group {} removed", placeHolder);
            } else {
                LOG.debug("Service group {} superseded by {}", placeHolder, serviceGroupMap.get(identifier.value()));
            }
            return;
        }

        // Placeholder is being retired, we are reusing its services as the seed for the group.
        final var group = createGroup(identifier, services);
        verify(serviceGroupMap.replace(identifier.value(), placeHolder, group));
        placeHolder.setSuccessor(group);
        LOG.debug("Service group upgraded from {} to {}", placeHolder, group);

        try {
            initializeOrRemoveGroup(group);
        } catch (CandidateAlreadyRegisteredException e) {
            LOG.error("Failed to register delayed group {}, it will remain inoperational", identifier, e);
        }
    }

    @Override
    public void ownershipChanged(final DOMEntity entity, final EntityOwnershipStateChange change,
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

    @VisibleForTesting
    static @NonNull String getServiceIdentifierFromEntity(final DOMEntity entity) {
        final var yii = entity.getIdentifier();
        final var niiwp = (NodeIdentifierWithPredicates) yii.getLastPathArgument();
        return niiwp.values().iterator().next().toString();
    }

    /**
     * Creates an extended {@link DOMEntity} instance.
     *
     * @param entityType the type of the entity
     * @param sgi the identifier of the entity
     * @return instance of Entity extended GenericEntity type
     */
    @VisibleForTesting
    static DOMEntity createEntity(final String entityType, final ServiceGroupIdentifier sgi) {
        return new DOMEntity(entityType, sgi.value());
    }
}
