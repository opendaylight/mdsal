/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nonnull;

/**
 * Common interface for implementing three-phase commit steps.
 * Actual steps to be implemented are: {@link PostCanCommitStep} and {@link PostPreCommitStep} which
 * allows to customize pre-commit, commit and abort actions.
 *
 */
@Beta
public interface ThreePhaseCommitStep {

    ListenableFuture<?> NOOP_ABORT_FUTURE = Futures.immediateFuture(null);

    /**
     * Invoked on transaction aborted.
     * This callback is invoked by three-phase commit coordinator if associated data transaction
     * will not be commited and is being aborted.
     * Implementation MUST rollback any changes, which were introduced by implementation based on
     * supplied data.
     *
     * @return ListenableFuture which will complete once abort is completed.
     */
    @Nonnull
    ListenableFuture<?> abort();
}
