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

public class ShardCommitCoordinationTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ShardCommitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final Collection<DOMStoreThreePhaseCommitCohort> cohorts;

    public ShardCommitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        this.rootShardPrefix = rootShardPrefix;
        this.cohorts = cohorts;
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        try {
            LOG.debug("Shard {}, canCommit started", rootShardPrefix);
            canCommitBlocking();

            LOG.debug("Shard {}, preCommit started", rootShardPrefix);
            preCommitBlocking();

            LOG.debug("Shard {}, commit started", rootShardPrefix);
            commitBlocking();

            return null;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Shard: {} Submit Error during phase {}, starting Abort", rootShardPrefix, e);
            //FIXME abort here
            throw e;
        }
    }

    private void canCommitBlocking() throws TransactionCommitFailedException {
        for (final ListenableFuture<?> canCommit : canCommitAll()) {
            try {
                final Boolean result = (Boolean)canCommit.get();
                if (result == null || !result) {
                    throw new TransactionCommitFailedException("Can Commit failed, no detailed cause available.");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new TransactionCommitFailedException("CanCommit failed", e);
            }
        }
    }

    private void preCommitBlocking() throws TransactionCommitFailedException {
        for (final ListenableFuture<?> preCommit : preCommitAll()) {
            try {
                preCommit.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new TransactionCommitFailedException("PreCommit failed", e);
            }
        }
    }

    private void commitBlocking() throws TransactionCommitFailedException {
        for (final ListenableFuture<?> commit : commitAll()) {
            try {
                commit.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new TransactionCommitFailedException("Commit failed", e);
            }
        }
    }

    private ListenableFuture<?>[] canCommitAll() {
        final ListenableFuture<?>[] ops = new ListenableFuture<?>[cohorts.size()];
        int i = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[i++] = cohort.canCommit();
        }
        return ops;
    }

    private ListenableFuture<?>[] preCommitAll() {
        final ListenableFuture<?>[] ops = new ListenableFuture<?>[cohorts.size()];
        int i = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[i++] = cohort.canCommit();
        }
        return ops;
    }

    private ListenableFuture<?>[] commitAll() {
        final ListenableFuture<?>[] ops = new ListenableFuture<?>[cohorts.size()];
        int i = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[i++] = cohort.canCommit();
        }
        return ops;
    }
}
