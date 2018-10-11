/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that coordinates the PreCommit phase of the provided {@link DOMStoreThreePhaseCommitCohort}'s.
 */
@Beta
public class ShardPreCommitCoordinationTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardPreCommitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final Collection<DOMStoreThreePhaseCommitCohort> cohorts;

    public ShardPreCommitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        this.rootShardPrefix = requireNonNull(rootShardPrefix);
        this.cohorts = requireNonNull(cohorts);
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        try {
            LOG.debug("Shard {}, preCommit started", rootShardPrefix);
            preCommitBlocking();

            return null;
        } catch (final TransactionCommitFailedException e) {
            LOG.warn("Shard: {} Submit Error during phase PreCommit, starting Abort", rootShardPrefix, e);
            //FIXME abort here
            throw e;
        }
    }

    void preCommitBlocking() throws TransactionCommitFailedException {
        for (final ListenableFuture<?> preCommit : preCommitAll()) {
            try {
                preCommit.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new TransactionCommitFailedException("PreCommit failed", e);
            }
        }
    }

    private ListenableFuture<?>[] preCommitAll() {
        final ListenableFuture<?>[] ops = new ListenableFuture<?>[cohorts.size()];
        int index = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[index++] = cohort.preCommit();
        }
        return ops;
    }

}
