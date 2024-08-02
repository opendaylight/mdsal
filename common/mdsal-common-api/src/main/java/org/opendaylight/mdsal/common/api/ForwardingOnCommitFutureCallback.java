/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link OnCommitFutureCallback} delegating to an {@link OnCommitFutureCallback}.
 */
@NonNullByDefault
public record ForwardingOnCommitFutureCallback(OnCommitCallback delegate) implements OnCommitFutureCallback {
    public ForwardingOnCommitFutureCallback {
        requireNonNull(delegate);
    }

    @Override
    public void onSuccess(final CommitInfo commitInfo) {
        delegate.onSuccess(commitInfo);
    }

    @Override
    public void onFailure(final TransactionCommitFailedException cause) {
        delegate.onFailure(cause);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
