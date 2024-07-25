/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.store;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Running;

/**
 * The {@link LogicalDatastoreType#CONFIGURATION} datastore.
 */
public non-sealed interface ConfigurationDatastore extends LogicalDatastore<Running> {
    @Override
    default Running datastore() {
        return Running.VALUE;
    }

    @Override
    default LogicalDatastoreType datastoreType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
