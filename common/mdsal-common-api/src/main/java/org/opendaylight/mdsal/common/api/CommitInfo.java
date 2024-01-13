/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for tagging information about a successful commit. This can include various ways of identifying
 * the resulting changeset, timing information or any other piece of data about the commit itself the implementation
 * deems interesting to the client.
 */
@Beta
@NonNullByDefault
public interface CommitInfo {
    /**
     * Return an empty {@link CommitInfo}.
     *
     * @return An empty {@link CommitInfo} instance.
     */
    static CommitInfo empty() {
        return DCI.EMPTY;
    }

    /**
     * Return an immediately-completed empty {@link CommitInfo} future.
     *
     * @return An empty {@link CommitInfo} instance enclosed in a completed future.
     */
    static FluentFuture<CommitInfo> emptyFluentFuture() {
        return DCI.EMPTY_FUTURE;
    }
}
