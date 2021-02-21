/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.DurationStatisticsTracker;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
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
        this.executor = requireNonNull(executor, "executor must not be null.");
    }

    public DurationStatisticsTracker getCommitStatsTracker() {
        return commitStatsTracker;
    }

    @Override
    protected FluentFuture<? extends CommitInfo> commit(final DOMDataTreeWriteTransaction transaction,
            final DOMStoreThreePhaseCommitCohort cohort) {
        LOG.debug("Tx: {} is submitted for execution.", transaction.getIdentifier());

        try {
            return FluentFuture.from(executor.submit(
                new CommitCoordinationTask.WithTracker(transaction, cohort, commitStatsTracker)));
        } catch (RejectedExecutionException e) {
            LOG.error("The commit executor's queue is full - submit task was rejected. \n{}", executor, e);
            return FluentFutures.immediateFailedFluentFuture(new TransactionCommitFailedException(
                "Could not submit the commit task - the commit queue capacity has been exceeded.", e));
        }
    }
}
