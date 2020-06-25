/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

final class Constants {
    /**
     * Subscribe request message. This is the only valid initial message in the sink->source direction. Its payload is
     * composed of a binary normalized node stream. The stream must contain a {@link LogicalDatastoreType} serialized
     * via {@link LogicalDatastoreType#writeTo(java.io.DataOutput)} followed by a single {@link YangInstanceIdentifier}.
     */
    static final byte MSG_SUBSCRIBE_REQ = 1;
    /**
     * Initial data indicating non-presence of the subscribed path.
     */
    static final byte MSG_EMPTY_DATA    = 2;
    /**
     * A chunk of the DataTreeCandidate serialization stream. May be followed by another chunk or
     * {@link #MSG_DTC_APPLY}.
     */
    static final byte MSG_DTC_CHUNK     = 3;
    /**
     * End-of-DataTreeCandidate serialization stream. The payload is empty.
     */
    static final byte MSG_DTC_APPLY     = 4;
    /**
     * Verify the connection is alive.
     */
    static final int MSG_PING           = 5;
    /**
     * Length of the length field in each transmitted frame.
     */
    static final int LENGTH_FIELD_LENGTH = 4;
    /**
     * Maximum frame size allowed by encoding, 1MiB.
     */
    static final int LENGTH_FIELD_MAX    = 1024 * 1024;

    static final ByteBuf EMPTY_DATA = Unpooled.unreleasableBuffer(
        Unpooled.wrappedBuffer(new byte[] { MSG_EMPTY_DATA }));

    static final ByteBuf PING = Unpooled.unreleasableBuffer(
        Unpooled.wrappedBuffer(new byte[] { MSG_PING }));

    static final ByteBuf DTC_APPLY = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(new byte[] { MSG_DTC_APPLY }));

    private Constants() {
        // Hidden on purpose
    }
}
