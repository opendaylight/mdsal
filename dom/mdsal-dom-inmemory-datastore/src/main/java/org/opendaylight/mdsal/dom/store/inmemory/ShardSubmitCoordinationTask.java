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
import java.util.Collection;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that coordinates all phases of transaction submit from the provided {@link DOMStoreThreePhaseCommitCohort}'s.
 * Each phase will only be started once all cohorts have finished the previous phase.
 */
@Beta
public class ShardSubmitCoordinationTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardSubmitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final ShardCanCommitCoordinationTask canCommitCoordinationTask;
    private final ShardPreCommitCoordinationTask preCommitCoordinationTask;
    private final ShardCommitCoordinationTask commitCoordinationTask;
    private final InmemoryDOMDataTreeShardWriteTransaction transaction;


    public ShardSubmitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts,
                                       final InmemoryDOMDataTreeShardWriteTransaction transaction) {
        this.rootShardPrefix = requireNonNull(rootShardPrefix);
        this.transaction = transaction;

        canCommitCoordinationTask = new ShardCanCommitCoordinationTask(rootShardPrefix, cohorts);
        preCommitCoordinationTask = new ShardPreCommitCoordinationTask(rootShardPrefix, cohorts);
        commitCoordinationTask = new ShardCommitCoordinationTask(rootShardPrefix, cohorts, transaction);
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        LOG.debug("Shard {}, tx{} CanCommit started", transaction.getIdentifier(), rootShardPrefix);
        canCommitCoordinationTask.canCommitBlocking();

        LOG.debug("Shard {}, tx{} PreCommit started", transaction.getIdentifier(), rootShardPrefix);
        preCommitCoordinationTask.preCommitBlocking();

        LOG.debug("Shard {}, tx{} commit started", transaction.getIdentifier(), rootShardPrefix);
        commitCoordinationTask.commitBlocking();

        transaction.transactionCommited(transaction);

        return null;
    }
}
