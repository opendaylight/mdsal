/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * The concept of a logical data store, similar to RFC8342.
 */
// FIXME: 6.0.0: turn this into an interface so it can be externally-defined?
// FIXME: 6.0.0: note that mount points can have different types and policies, which can potentially be mapped
public enum LogicalDatastoreType implements WritableObject {
    /**
     * Logical datastore representing operational state of the system and it's components. This datastore is used
     * to describe operational state of the system and it's operation related data.
     *
     * <p>
     * It is defined to:
     * <ul>
     *   <li>contain both {@code config=true} and {@code config=false} nodes</li>
     *   <li>be replicated across all nodes by default, individual shards may have different strategies, which need to
     *       be documented
     *   </li>
     * </ul>
     */
    OPERATIONAL(1),
    /**
     * Logical Datastore representing configuration state of the system and it's components. This datastore is used
     * to describe intended state of the system and intended operation mode.
     *
     * <p>
     * It is defined to:
     * <ul>
     *   <li>contain only {@code config=true} nodes</li>
     *   <li>be replicated across all nodes by default, individual shards may have different strategies, which need to
     *       be documented
     *   </li>
     *   <li>be persisted on all nodes by default, individual shards may have different strategies, which need to
     *       be documented
     *   </li>
     * </ul>
     */
    CONFIGURATION(2);

    private int serialized;

    LogicalDatastoreType(final int serialized) {
        this.serialized = serialized;
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeByte(serialized);
    }

    public static @NonNull LogicalDatastoreType readFrom(final DataInput in) throws IOException {
        final byte serialized = in.readByte();
        switch (serialized) {
            case 1:
                return OPERATIONAL;
            case 2:
                return CONFIGURATION;
            default:
                throw new IOException("Unknown type " + serialized);
        }
    }
}
