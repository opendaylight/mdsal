/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, property = "type=default")
public final class OSGiClusterSingletonServiceProvider implements ClusterSingletonServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiClusterSingletonServiceProvider.class);

    private DOMClusterSingletonServiceProviderImpl delegate;

    @Activate
    public OSGiClusterSingletonServiceProvider(@Reference final DOMEntityOwnershipService entityOwnershipService) {
        LOG.info("Cluster Singleton Service starting");
        delegate = new DOMClusterSingletonServiceProviderImpl(entityOwnershipService);
        delegate.initializeProvider();
        LOG.info("Cluster Singleton Service started");
    }

    @Deactivate
    void deactivate() {
        LOG.info("Cluster Singleton Service stopping");
        delegate.close();
        delegate = null;
        LOG.info("Cluster Singleton Service stopped");
    }

    @Override
    public ClusterSingletonServiceRegistration registerClusterSingletonService(final ClusterSingletonService service) {
        return delegate.registerClusterSingletonService(service);
    }

    @Override
    public void close() {
        // Ignored on purpose
    }
}
