/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public class ForeignShardThreePhaseCommitCohort implements DOMStoreThreePhaseCommitCohort {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignShardThreePhaseCommitCohort.class);

    private final DOMDataTreeIdentifier prefix;
    private final ForeignShardModificationContext shard;

    public ForeignShardThreePhaseCommitCohort(final DOMDataTreeIdentifier prefix,
            final ForeignShardModificationContext shard) {
        this.prefix = requireNonNull(prefix);
        this.shard = requireNonNull(shard);
    }

    @Override
    public ListenableFuture<Boolean> canCommit() {
        LOG.debug("Validating transaction on foreign shard {}", prefix);
        return shard.isModified() ? shard.validate() : FluentFutures.immediateTrueFluentFuture();
    }

    @Override
    public ListenableFuture<Void> preCommit() {
        LOG.debug("Preparing transaction on foreign shard {}", prefix);
        return shard.isModified() ? shard.prepare() : FluentFutures.immediateNullFluentFuture();
    }

    @Override
    public ListenableFuture<Void> abort() {
        LOG.debug("Aborting transaction of foreign shard {}", prefix);
        shard.closeForeignTransaction();
        return FluentFutures.immediateNullFluentFuture();
    }

    @Override
    public ListenableFuture<Void> commit() {
        LOG.debug("Submitting transaction on foreign shard {}", prefix);
        return shard.isModified() ? shard.submit() : FluentFutures.immediateNullFluentFuture();
    }
}