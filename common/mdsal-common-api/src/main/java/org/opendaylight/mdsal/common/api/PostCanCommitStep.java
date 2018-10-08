/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

/**
 * User implementation of steps following can-commit in three phase protocol.
 * If no additional visibility into transaction and data being aborted or committed is needed, use
 * {@link #NOOP} implementation.
 */
@Beta
public interface PostCanCommitStep extends ThreePhaseCommitStep {
    /**
     * No-op implementation of abort, pre-commit and commit steps.
     * This implementation should be used if user logic does only validation of data and does not
     * need to perform any actions associated with pre-commit, commit or abort.
     */
    PostCanCommitStep NOOP = new PostCanCommitStep() {

        @Override
        public ListenableFuture<?> abort() {
            return ThreePhaseCommitStep.NOOP_ABORT_FUTURE;
        }

        @Override
        public ListenableFuture<? extends PostPreCommitStep> preCommit() {
            return PostPreCommitStep.NOOP_FUTURE;
        }
    };

    @NonNull FluentFuture<PostCanCommitStep> NOOP_SUCCESSFUL_FUTURE = FluentFutures.immediateFluentFuture(NOOP);

    /**
     * Initiates a pre-commit of associated request
     * Implementation MUST NOT do any blocking calls during this callback, all pre-commit
     * preparation SHOULD happen asynchronously and MUST result in completing returned future
     * object.
     *
     * @return Future which is completed once pre-commit phase for this request is finished.
     */
    @NonNull ListenableFuture<? extends PostPreCommitStep> preCommit();
}
