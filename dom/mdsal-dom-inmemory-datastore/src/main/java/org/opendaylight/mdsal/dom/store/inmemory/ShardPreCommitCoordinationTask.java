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

class ShardPreCommitCoordinationTask implements Callable<Void>{

    private static final Logger LOG = LoggerFactory.getLogger(ShardPreCommitCoordinationTask.class);

    private final DOMDataTreeIdentifier rootShardPrefix;
    private final Collection<DOMStoreThreePhaseCommitCohort> cohorts;

    ShardPreCommitCoordinationTask(final DOMDataTreeIdentifier rootShardPrefix,
                                       final Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
        this.rootShardPrefix = rootShardPrefix;
        this.cohorts = cohorts;
    }

    @Override
    public Void call() throws TransactionCommitFailedException {

        try {
            LOG.debug("Shard {}, preCommit started", rootShardPrefix);
            preCommitBlocking();

            return null;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Shard: {} Submit Error during phase {}, starting Abort", rootShardPrefix, e);
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
        int i = 0;
        for (final DOMStoreThreePhaseCommitCohort cohort : cohorts) {
            ops[i++] = cohort.preCommit();
        }
        return ops;
    }

}
