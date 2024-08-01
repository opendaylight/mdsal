/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A callback invoked when a datastore transaction completes.
 */
@NonNullByDefault
public interface OnCommitCallback {
    /**
     * Invoked with the {@link CommitInfo} information when the transaction is successful.
     *
     * @param commitInfo information about the datastore state at the point when the transaction succeeded
     */
    void onSuccess(CommitInfo commitInfo);

    /**
     * Invoked with the {@link TransactionCommitFailedException} cause when the transaction fails..
     */
    void onFailure(TransactionCommitFailedException cause);
}