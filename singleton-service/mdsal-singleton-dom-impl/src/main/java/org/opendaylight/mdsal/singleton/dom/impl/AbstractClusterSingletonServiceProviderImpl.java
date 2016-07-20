/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.CommonClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class {@link AbstractClusterSingletonServiceProviderImpl} represents implementations of
 * {@link CommonClusterSingletonServiceProvider} and it implements {@link GenericEntityOwnershipListener}
 * for providing OwnershipChange for all registered {@link ClusterSingletonServiceGroup} entity
 * candidate.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 * @param <G> the GenericEntityOwnershipListener type
 * @param <S> the GenericEntityOwnershipService type
 * @param <R> the GenericEntityOwnershipListenerRegistration type
 */
public abstract class AbstractClusterSingletonServiceProviderImpl<P extends Path<P>, E extends GenericEntity<P>,
                                                                  C extends GenericEntityOwnershipChange<P, E>,
                                                                  G extends GenericEntityOwnershipListener<P, C>,
                                                                  S extends GenericEntityOwnershipService<P, E, G>,
                                                                  R extends GenericEntityOwnershipListenerRegistration<P, G>>
        implements CommonClusterSingletonServiceProvider, GenericEntityOwnershipListener<P, C> {

    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractClusterSingletonServiceProviderImpl.class.getName());

    private static final String SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";
    private static final String CLOSE_SERVICE_ENTITY_TYPE = "org.opendaylight.mdsal.AsyncServiceCloseEntityType";

    private final S entityOwnershipService;
    private final ConcurrentMap<String, ClusterSingletonServiceGroup<P, E, C>> serviceGroupMap = new ConcurrentHashMap<>();

    /* EOS Entity Listeners Registration */
    private R serviceEntityListenerReg;
    private R asyncCloseEntityListenerReg;

    /**
     * Class constructor
     *
     * @param entityOwnershipService relevant EOS
     */
    protected AbstractClusterSingletonServiceProviderImpl(@Nonnull final S entityOwnershipService) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
    }

    /**
     * This method must be called once on startup to initialize this provider.
     */
    public final void initializeProvider() {
        LOG.debug("Initialization method for ClusterSingletonService Provider {}", this.getClass().getName());
        this.serviceEntityListenerReg = registerListener(SERVICE_ENTITY_TYPE, entityOwnershipService);
        this.asyncCloseEntityListenerReg = registerListener(CLOSE_SERVICE_ENTITY_TYPE, entityOwnershipService);
    }

    @Override
    public final ClusterSingletonServiceRegistration registerClusterSingletonService(
            @CheckForNull final ClusterSingletonService service) {
        LOG.debug("Call registrationService {} method for ClusterSingletonService Provider {}", service,
                this.getClass().getName());

        Preconditions.checkArgument(service != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(service.getIdentifier().getValue()),
                "ClusterSingletonService idetnifier can not be null. {}", service);

        final String serviceIdentifier = service.getIdentifier().getValue();
        ClusterSingletonServiceGroup<P, E, C> serviceGroup = serviceGroupMap.get(serviceIdentifier);
        if (serviceGroup == null) {
            final E mainEntity = createEntity(SERVICE_ENTITY_TYPE, serviceIdentifier);
            final E closeEntity = createEntity(CLOSE_SERVICE_ENTITY_TYPE, serviceIdentifier);
            serviceGroup = new ClusterSingletonServiceGroupImpl<>(serviceIdentifier,
                    mainEntity, closeEntity, entityOwnershipService, serviceGroupMap);
            serviceGroupMap.put(service.getIdentifier().getValue(), serviceGroup);
            serviceGroup.initializationClusterSingletonGroup();
        }
        return serviceGroup.registerService(service);
    }

    @Override
    public final void close() {
        LOG.debug("Close method for ClusterSingletonService Provider {}", this.getClass().getName());

        if (serviceEntityListenerReg != null) {
            serviceEntityListenerReg.close();
            serviceEntityListenerReg = null;
        }

        final List<ListenableFuture<List<Void>>> listGroupCloseListFuture = new ArrayList<>();

        for (final ClusterSingletonServiceGroup<P, E, C> serviceGroup : serviceGroupMap.values()) {
            listGroupCloseListFuture.add(serviceGroup.closeClusterSingletonGroup());
        }

        final ListenableFuture<List<List<Void>>> finalCloseFuture = Futures.allAsList(listGroupCloseListFuture);
        Futures.addCallback(finalCloseFuture, new FutureCallback<List<List<Void>>>() {

            @Override
            public void onSuccess(final List<List<Void>> result) {
                cleaningProvider(null);
            }

            @Override
            public void onFailure(final Throwable t) {
                cleaningProvider(t);
            }
        });
    }

    @Override
    public final void ownershipChanged(final C ownershipChange) {
        LOG.debug("Ownership change for ClusterSingletonService Provider {}", ownershipChange);
        final String serviceIdentifier = getServiceIdentifierFromEntity(ownershipChange.getEntity());
        final ClusterSingletonServiceGroup<P, E, C> serviceHolder = serviceGroupMap.get(serviceIdentifier);
        if (serviceHolder != null) {
            serviceHolder.ownershipChanged(ownershipChange);
        } else {
            LOG.debug("ClusterSingletonServiceGroup was not found for serviceIdentifier {}", serviceIdentifier);
        }
    }

    /**
     * Method implementation registers a defined {@link GenericEntityOwnershipListenerRegistration} type
     * EntityOwnershipListenerRegistration.
     *
     * @param entityType the type of the entity
     * @param entityOwnershipServiceInst - EOS type
     * @return instance of EntityOwnershipListenerRegistration
     */
    protected abstract R registerListener(final String entityType, final S entityOwnershipServiceInst);

    /**
     * Creates an extended {@link GenericEntity} instance.
     *
     * @param entityType the type of the entity
     * @param entityIdentifier the identifier of the entity
     * @return instance of Entity extended GenericEntity type
     */
    protected abstract E createEntity(final String entityType, final String entityIdentifier);

    /**
     * Method is responsible for parsing ServiceGroupIdentifier from E entity.
     *
     * @param entity
     * @return ServiceGroupIdentifier parsed from entity key value.
     */
    protected abstract String getServiceIdentifierFromEntity(final E entity);

    /**
     * Method is called async. from close method in end of Provider lifecycle.
     *
     * @param t Throwable (needs for log)
     */
    protected final void cleaningProvider(@Nullable final Throwable t) {
        LOG.debug("Final cleaning ClusterSingletonServiceProvider {}", this.getClass().getName());
        if (t != null) {
            LOG.warn("Unexpected problem by closing ClusterSingletonServiceProvider {}", this.getClass().getName(), t);
        }
        if (asyncCloseEntityListenerReg != null) {
            asyncCloseEntityListenerReg.close();
            asyncCloseEntityListenerReg = null;
        }
        serviceGroupMap.clear();
    }
}
