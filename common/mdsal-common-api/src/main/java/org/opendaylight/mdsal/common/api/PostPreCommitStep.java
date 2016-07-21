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

/**
 * User implementation of steps following pre-commit from Three-Phase Protocol.
 *
 */
@Beta
public interface PostPreCommitStep extends ThreePhaseCommitStep {

    /**
     * No-op implementation of {@link #abort()} and {@link #commit()} method, which always success
     * calls.
     * This implementation is intended for users which may not need to implement commit and abort
     * method.
     *
     */
    PostPreCommitStep NOOP = new PostPreCommitStep() {

        @Override
        public ListenableFuture<?> abort() {
            return ThreePhaseCommitStep.NOOP_ABORT_FUTURE;
        }

        @Override
        public ListenableFuture<?> commit() {
            return NOOP_COMMIT_FUTURE;
        }
    };

    ListenableFuture<?> NOOP_COMMIT_FUTURE = Futures.immediateFuture(null);

    ListenableFuture<? extends PostPreCommitStep> NOOP_FUTURE = Futures.immediateFuture(NOOP);

    /**
     * Commits cohort transaction.
     * This callback is invoked by three-phase commit coordinator if associated data transaction
     * finished pre-commit phase and will be commited.
     * Implementation should make state, which were derived by implementation from associated data
     * visible.
     *
     * @return Listenable Future which will complete once commit is finished.
     */
    ListenableFuture<?> commit();

}
