/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShardCommitCoordinationTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardCommitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final Collection<DOMStoreThreePhaseCommitCohort> cohorts;

    ShardCommitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        this.rootShardPrefix = rootShardPrefix;
        this.cohorts = cohorts;
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        try {
            LOG.debug("Shard {}, commit started", rootShardPrefix);
            commitBlocking();

            return null;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Shard: {} Submit Error during phase Commit, starting Abort", rootShardPrefix, e);
            //FIXME abort here
            throw e;
        }
    }

    void commitBlocking() throws TransactionCommitFailedException {
        for (final ListenableFuture<?> commit : commitAll()) {
            try {
                commit.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new TransactionCommitFailedException("Commit failed", e);
            }
        }
    }

    private ListenableFuture<?>[] commitAll() {
        final ListenableFuture<?>[] ops = new ListenableFuture<?>[cohorts.size()];
        int i = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[i++] = cohort.commit();
        }
        return ops;
    }
}
