/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.store;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;

/**
 * Provides access to a particular datastore.
 *
 * @param <D> a {@link Datastore} type
 */
public sealed interface DatastoreAccess<D extends Datastore> permits LogicalDatastore {
    /**
     * The {@link Datastore} exposed via this instance.
     *
     * @return the {@link Datastore} exposed via this instance
     */
    @NonNull D datastore();
}
