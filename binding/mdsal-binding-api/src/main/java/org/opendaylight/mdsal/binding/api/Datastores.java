/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Running;

public final class Datastores {
    private static final Map<Datastore, LogicalDatastoreType> DATASTORES = Map.of(
        Running.VALUE, LogicalDatastoreType.CONFIGURATION,
        Operational.VALUE, LogicalDatastoreType.OPERATIONAL);

    public static @NonNull Datastore ofType(final LogicalDatastoreType type) {
        return switch (type) {
            case CONFIGURATION -> Running.VALUE;
            case OPERATIONAL -> Operational.VALUE;
        };
    }

    public static @Nullable LogicalDatastoreType typeOf(final Datastore datastore) {
        return DATASTORES.get(requireNonNull(datastore));
    }

    public static @NonNull LogicalDatastoreType verifyTypeOf(final Datastore datastore) {
        return verifyNotNull(typeOf(datastore), "Unhandled datastore %s", datastore);
    }

    private Datastores() {
        // Hidden on purpose
    }
}
