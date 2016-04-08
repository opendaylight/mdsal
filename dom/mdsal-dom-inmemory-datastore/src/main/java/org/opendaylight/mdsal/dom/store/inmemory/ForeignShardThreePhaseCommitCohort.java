package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForeignShardThreePhaseCommitCohort implements DOMStoreThreePhaseCommitCohort {

    private static final Logger LOG = LoggerFactory.getLogger(ForeignShardThreePhaseCommitCohort.class);

    private final DOMDataTreeIdentifier prefix;
    private final ForeignShardModificationContext shard;

    public ForeignShardThreePhaseCommitCohort(final DOMDataTreeIdentifier prefix, final ForeignShardModificationContext shard) {
        this.prefix = prefix;
        this.shard = shard;
    }

    @Override
    public ListenableFuture<Boolean> canCommit() {
        LOG.debug("Validating transaction on foreign shard {}", prefix);
        return shard.validate();
    }

    @Override
    public ListenableFuture<Void> preCommit() {
        LOG.debug("Preparing transaction on foreign shard {}", prefix);
        return shard.prepare();
    }

    @Override
    public ListenableFuture<Void> abort() {
        // FIXME abort on the shard
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<Void> commit() {
        LOG.debug("Submitting transaction on foreign shard {}", prefix);
        return shard.submit();
    }
}