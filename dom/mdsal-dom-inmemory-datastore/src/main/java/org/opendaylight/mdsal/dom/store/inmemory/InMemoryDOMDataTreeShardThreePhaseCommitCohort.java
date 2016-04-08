/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryDOMDataTreeShardThreePhaseCommitCohort implements DOMStoreThreePhaseCommitCohort {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShardThreePhaseCommitCohort.class);
    private static final ListenableFuture<Void> SUCCESSFUL_FUTURE = Futures.immediateFuture(null);
    private static final ListenableFuture<Boolean> CAN_COMMIT_FUTURE = Futures.immediateFuture(Boolean.TRUE);

    private final DataTree dataTree;
    private final DataTreeModification modification;
    private final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> subshards;
    private DataTreeCandidate candidate;

    InMemoryDOMDataTreeShardThreePhaseCommitCohort(final DataTree dataTree,
                                                   final DataTreeModification modification,
                                                   final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> subshards) {
        this.dataTree = dataTree;
        this.modification = modification;
        this.subshards = subshards;
    }

    @Override
    public ListenableFuture<Boolean> canCommit() {
        try {
            dataTree.validate(modification);
            LOG.debug("DataTreeModification {} validated");

            if (subshards.isEmpty()) {
                return CAN_COMMIT_FUTURE;
            }

            final ArrayList<ListenableFuture<Boolean>> futures = new ArrayList<>();
            // gather futures and return future based on those
            for (Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> subshard : subshards.entrySet()) {
                futures.add(subshard.getValue().validate());
            }

            final SettableFuture<Boolean> resultFuture = SettableFuture.create();
            final ListenableFuture<List<Boolean>> validationFuture = Futures.allAsList(futures);
            Futures.addCallback(validationFuture, new FutureCallback<List<Boolean>>() {
                @Override
                public void onSuccess(List<Boolean> result) {
                    resultFuture.set(true);
                }

                @Override
                public void onFailure(Throwable t) {
                    resultFuture.setException(t);
                }
            });

            return resultFuture;
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

            if (subshards.isEmpty()) {
                return SUCCESSFUL_FUTURE;
            }

            final ArrayList<ListenableFuture<Void>> futures = new ArrayList<>();
            // gather futures and return future based on those
            for (Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> subshard : subshards.entrySet()) {
                futures.add(subshard.getValue().prepare());
            }

            final SettableFuture<Void> resultFuture = SettableFuture.create();
            final ListenableFuture<List<Void>> validationFuture = Futures.allAsList(futures);
            Futures.addCallback(validationFuture, new FutureCallback<List<Void>>() {
                @Override
                public void onSuccess(List<Void> result) {
                    resultFuture.set(null);
                }

                @Override
                public void onFailure(Throwable t) {
                    resultFuture.setException(t);
                }
            });

            return resultFuture;
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
        Preconditions.checkNotNull(candidate);

        dataTree.commit(candidate);

        if (subshards.isEmpty()) {
            return SUCCESSFUL_FUTURE;
        }

        final ArrayList<ListenableFuture<Void>> futures = new ArrayList<>();
        // gather futures and return future based on those
        for (Entry<DOMDataTreeIdentifier, ForeignShardModificationContext> subshard : subshards.entrySet()) {
            futures.add(subshard.getValue().submit());
        }

        final SettableFuture<Void> resultFuture = SettableFuture.create();
        final ListenableFuture<List<Void>> validationFuture = Futures.allAsList(futures);
        Futures.addCallback(validationFuture, new FutureCallback<List<Void>>() {
            @Override
            public void onSuccess(List<Void> result) {
                resultFuture.set(null);
            }

            @Override
            public void onFailure(Throwable t) {
                resultFuture.setException(t);
            }
        });

        return resultFuture;
    }
}
