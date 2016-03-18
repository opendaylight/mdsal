/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShardSubmitCoordinationTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardSubmitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final ShardCanCommitCoordinationTask canCommitCoordinationTask;
    private final ShardPreCommitCoordinationTask preCommitCoordinationTask;
    private final ShardCommitCoordinationTask commitCoordinationTask;


    ShardSubmitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        this.rootShardPrefix = rootShardPrefix;

        canCommitCoordinationTask = new ShardCanCommitCoordinationTask(rootShardPrefix, cohorts);
        preCommitCoordinationTask = new ShardPreCommitCoordinationTask(rootShardPrefix, cohorts);
        commitCoordinationTask = new ShardCommitCoordinationTask(rootShardPrefix, cohorts);
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        LOG.debug("Shard {}, CanCommit started", rootShardPrefix);
        canCommitCoordinationTask.canCommitBlocking();

        LOG.debug("Shard {}, PreCommit started", rootShardPrefix);
        preCommitCoordinationTask.preCommitBlocking();

        LOG.debug("Shard {}, commit started", rootShardPrefix);
        commitCoordinationTask.commitBlocking();

        return null;
    }
}
