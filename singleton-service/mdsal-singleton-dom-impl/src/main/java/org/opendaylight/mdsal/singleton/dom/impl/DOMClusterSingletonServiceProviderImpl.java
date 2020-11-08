/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import java.util.ServiceLoader;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Binding version of {@link AbstractClusterSingletonServiceProviderImpl}.
 */
@MetaInfServices(value = ClusterSingletonServiceProvider.class)
public class DOMClusterSingletonServiceProviderImpl extends
        AbstractClusterSingletonServiceProviderImpl<YangInstanceIdentifier, DOMEntity,
                                                    DOMEntityOwnershipChange,
                                                    DOMEntityOwnershipListener,
                                                    DOMEntityOwnershipService,
                                                    DOMEntityOwnershipListenerRegistration>
        implements DOMEntityOwnershipListener {

    public DOMClusterSingletonServiceProviderImpl() {
        this(ServiceLoader.load(DOMEntityOwnershipService.class).findFirst().orElseThrow(
            () -> new IllegalStateException("Could not find DOMEntityOwnershipService")));
    }

    protected DOMClusterSingletonServiceProviderImpl(final DOMEntityOwnershipService entityOwnershipService) {
        super(entityOwnershipService);
    }

    @Override
    protected DOMEntity createEntity(final String type, final String ident) {
        return new DOMEntity(type, ident);
    }

    @Override
    protected DOMEntityOwnershipListenerRegistration registerListener(final String type,
            final DOMEntityOwnershipService eos) {
        return eos.registerListener(type, this);
    }

    @Override
    protected String getServiceIdentifierFromEntity(final DOMEntity entity) {
        final YangInstanceIdentifier yii = entity.getIdentifier();
        final NodeIdentifierWithPredicates niiwp = (NodeIdentifierWithPredicates) yii.getLastPathArgument();
        return niiwp.values().iterator().next().toString();
    }
}
