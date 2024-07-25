/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.store;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;

/**
 * Compatibility interface tying {@link DatastoreAccess} to {@link LogicalDatastoreType}.
 */
public sealed interface LogicalDatastore<D extends Datastore> extends DatastoreAccess<D>
        permits ConfigurationDatastore, OperationalDatastore {
    /**
     * Returns the {@link LogicalDatastoreType}.
     *
     * @return the {@link LogicalDatastoreType}
     */
    @NonNull LogicalDatastoreType datastoreType();
}
