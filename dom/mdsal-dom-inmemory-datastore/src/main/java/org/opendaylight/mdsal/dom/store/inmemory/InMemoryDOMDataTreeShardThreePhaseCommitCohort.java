/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InMemoryDOMDataTreeShardThreePhaseCommitCohort implements DOMStoreThreePhaseCommitCohort {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShardThreePhaseCommitCohort.class);
    private static final ListenableFuture<Void> SUCCESSFUL_FUTURE = Futures.immediateFuture(null);
    private static final ListenableFuture<Boolean> CAN_COMMIT_FUTURE = Futures.immediateFuture(Boolean.TRUE);

    private final DataTree dataTree;
    private final DataTreeModification modification;
    private DataTreeCandidate candidate;
    private final InMemoryDOMDataTreeShardChangePublisher changePublisher;

    InMemoryDOMDataTreeShardThreePhaseCommitCohort(final DataTree dataTree,
                                                   final DataTreeModification modification,
                                                   final InMemoryDOMDataTreeShardChangePublisher changePublisher) {
        this.dataTree = Preconditions.checkNotNull(dataTree);
        this.modification = Preconditions.checkNotNull(modification);
        this.changePublisher = Preconditions.checkNotNull(changePublisher);
    }

    @Override
    public ListenableFuture<Boolean> canCommit() {
        try {
            dataTree.validate(modification);
            LOG.debug("DataTreeModification {} validated", modification);

            return CAN_COMMIT_FUTURE;
        } catch (DataValidationFailedException e) {
            LOG.warn("Data validation failed for {}", modification);
            LOG.trace("dataTree : {}", dataTree);

            return Futures.immediateFailedFuture(new TransactionCommitFailedException("Data did not pass validation.", e));
        } catch (Exception e) {
            LOG.warn("Unexpected failure in validation phase", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public ListenableFuture<Void> preCommit() {
        try {
            candidate = dataTree.prepare(modification);
            LOG.debug("DataTreeModification {} prepared", modification);
            return SUCCESSFUL_FUTURE;
        } catch (Exception e) {
            LOG.warn("Unexpected failure in preparation phase", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public ListenableFuture<Void> abort() {
        candidate = null;
        return SUCCESSFUL_FUTURE;
    }

    @Override
    public ListenableFuture<Void> commit() {
        Preconditions.checkState(candidate != null, "Attempted to commit an aborted transaction");
        LOG.debug("Commiting candidate {}", candidate);
        dataTree.commit(candidate);
        // publish this change for listeners
        changePublisher.publishChange(candidate);
        return SUCCESSFUL_FUTURE;
    }
}
