/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import java.io.ObjectStreamException;
import java.time.Instant;
import java.util.UUID;

/**
 * Empty commit info singleton. Useful when {@link AsyncWriteTransaction#commit()} has nothing more to say.
 */
record EmptyCommitInfo(Instant commitTime, UUID commitUUID) implements CommitInfo {
    @java.io.Serial
    private static final long serialVersionUID = 0L;

    static final EmptyCommitInfo EMPTY = new EmptyCommitInfo(null, null);
    static final FluentFuture<CommitInfo> EMPTY_FUTURE = FluentFuture.from(Futures.immediateFuture(EMPTY));

    @java.io.Serial
    @SuppressWarnings("static-method")
    Object readResolve() throws ObjectStreamException {
        return commitTime != null || commitUUID != null ? this : EMPTY;
    }
}
