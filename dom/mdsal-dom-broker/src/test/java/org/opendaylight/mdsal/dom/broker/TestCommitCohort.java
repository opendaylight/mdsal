/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;

public enum TestCommitCohort implements DOMStoreThreePhaseCommitCohort {


    ALLWAYS_SUCCESS(true, true, true, true), CAN_COMMIT_FAILED(false, false, false, true), PRE_COMMIT_FAILED(true,
            false, false, true), COMMIT_FAILED(true, true, false, true);
    ;



    private TestCommitCohort(final boolean canCommit, final boolean preCommit, final boolean commit, final boolean abort) {
        this.canCommit = Futures.immediateFuture(canCommit);
        this.preCommit = immediate(canCommit, new IllegalStateException());
        this.commit = immediate(commit, new IllegalStateException());
        this.abort = immediate(abort, new IllegalStateException());
    }


    private final ListenableFuture<Boolean> canCommit;
    private final ListenableFuture<Void> preCommit;
    private final ListenableFuture<Void> commit;
    private final ListenableFuture<Void> abort;

    @Override
    public ListenableFuture<Boolean> canCommit() {
        return canCommit;
    }

    @Override
    public ListenableFuture<Void> preCommit() {
        return preCommit;
    }

    @Override
    public ListenableFuture<Void> abort() {
        return abort;
    }

    @Override
    public ListenableFuture<Void> commit() {
        return commit;
    }

    private static ListenableFuture<Void> immediate(final boolean isSuccess, final Exception e) {
        return isSuccess ? Futures.<Void>immediateFuture(null) : Futures.<Void>immediateFailedFuture(e);
    }



}
