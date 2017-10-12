/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.MappingCheckedFuture;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.DurationStatisticsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of blocking three phase commit coordinator, which which
 * supports coordination on multiple {@link DOMStoreThreePhaseCommitCohort}.
 *
 *<p>
 * This implementation does not support cancellation of commit,
 *
 *<p>
 * In order to advance to next phase of three phase commit all subtasks of
 * previous step must be finish.
 *
 *<p>
 * This executor does not have an upper bound on subtask timeout.
 */
public class SerializedDOMDataBroker extends AbstractDOMDataBroker {
    private static final Logger LOG = LoggerFactory.getLogger(SerializedDOMDataBroker.class);
    private final DurationStatisticsTracker commitStatsTracker = DurationStatisticsTracker.createConcurrent();
    private final ListeningExecutorService executor;

    /**
     * Construct DOMDataCommitCoordinator which uses supplied executor to
     * process commit coordinations.
     *
     * @param datastores the Map of backing DOMStore instances
     * @param executor the ListeningExecutorService to use
     */
    public SerializedDOMDataBroker(final Map<LogicalDatastoreType, DOMStore> datastores,
            final ListeningExecutorService executor) {
        super(datastores);
        this.executor = Preconditions.checkNotNull(executor, "executor must not be null.");
    }

    public DurationStatisticsTracker getCommitStatsTracker() {
        return commitStatsTracker;
    }

    @Override
    protected CheckedFuture<Void,TransactionCommitFailedException> submit(
            final DOMDataTreeWriteTransaction transaction,
            final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        Preconditions.checkArgument(transaction != null, "Transaction must not be null.");
        Preconditions.checkArgument(cohorts != null, "Cohorts must not be null.");
        LOG.debug("Tx: {} is submitted for execution.", transaction.getIdentifier());

        ListenableFuture<Void> commitFuture = null;
        try {
            commitFuture = executor.submit(new CommitCoordinationTask(transaction, cohorts,
                    commitStatsTracker));
        } catch (RejectedExecutionException e) {
            LOG.error("The commit executor's queue is full - submit task was rejected. \n"
                    + executor, e);
            return Futures.immediateFailedCheckedFuture(
                    new TransactionCommitFailedException(
                        "Could not submit the commit task - the commit queue capacity has been exceeded.", e));
        }

        return MappingCheckedFuture.create(commitFuture,
                TransactionCommitFailedExceptionMapper.COMMIT_ERROR_MAPPER);
    }
}
