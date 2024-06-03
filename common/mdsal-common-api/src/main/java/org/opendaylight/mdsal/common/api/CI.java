/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import java.io.ObjectStreamException;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Default {@link CommitInfo} implementation.
 */
record CI(UUID uuid, Instant instant) implements CommitInfo {
    static final @NonNull CI EMPTY = new CI(null, null);
    static final @NonNull FluentFuture<@NonNull CommitInfo> EMPTY_FUTURE =
        FluentFuture.from(Futures.immediateFuture(EMPTY));

    static @NonNull CI of(final UUID uuid, final Instant time) {
        return time != null || uuid != null ? new CI(uuid, time) : EMPTY;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CommitInfo.class).omitNullValues()
            .add("uuid", uuid)
            .add("instant", instant)
            .toString();
    }

    @java.io.Serial
    private Object readResolve() throws ObjectStreamException {
        return instant != null || uuid != null ? this : EMPTY;
    }
}
