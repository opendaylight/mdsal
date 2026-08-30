/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObjectReference;

/**
 * A serialization proxy for {@link DataTreeIdentifier}.
 */
@NonNullByDefault
record DTIv2(byte datastore, DataObjectReference<?> path) implements Serializable {
    DTIv2 {
        switch (datastore) {
            case 1, 2 -> {
                // okay
            }
            default -> throw iae(datastore);
        }
        requireNonNull(path);
    }

    DTIv2(final DataTreeIdentifier<?> id) {
        this(switch (id.datastore()) {
            case CONFIGURATION -> (byte) 1;
            case OPERATIONAL -> (byte) 2;
        }, id.path().toReference());
    }

    @java.io.Serial
    Object readResolve() {
        return DataTreeIdentifier.of(switch (datastore) {
            case 1 -> LogicalDatastoreType.CONFIGURATION;
            case 2 -> LogicalDatastoreType.OPERATIONAL;
            default -> throw iae(datastore);
        }, path);
    }

    private static IllegalArgumentException iae(final byte datastore) {
        return new IllegalArgumentException("Invalid datastore value" + datastore);
    }
}
