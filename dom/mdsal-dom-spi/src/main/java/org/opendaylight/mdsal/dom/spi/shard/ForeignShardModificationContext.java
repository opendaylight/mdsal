/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@NotThreadSafe
public final class ForeignShardModificationContext implements Identifiable<DOMDataTreeIdentifier> {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignShardModificationContext.class);

    private final DOMDataTreeIdentifier identifier;
    private final DOMDataTreeShardProducer producer;

    private DOMDataTreeShardWriteTransaction tx;
    private DOMDataTreeWriteCursor cursor;

    private volatile boolean notReady = true;

    public ForeignShardModificationContext(final DOMDataTreeIdentifier identifier,
                                           final DOMDataTreeShardProducer producer) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.producer = Preconditions.checkNotNull(producer);
    }

    public DOMDataTreeWriteCursor getCursor() {
        Preconditions.checkState(notReady, "Context %s has been readied", this);

        if (cursor == null) {
            if (tx == null) {
                tx = producer.createTransaction();
            }
            cursor = tx.createCursor(getIdentifier());
        }
        return cursor;
    }

    public boolean isModified() {
        return tx != null;
    }

    public void ready() {
        if (!notReady) {
            // Idempotent, but emit a debug
            LOG.debug("Duplicate ready() of context {}", this);
            return;
        }

        notReady = true;
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (tx != null) {
            tx.ready();
            // TODO: it would be nice if we could clear this reference
            // tx = null;
        }
    }

    @Override
    public DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }

    public ListenableFuture<Boolean> validate() {
        return tx.validate();
    }

    public ListenableFuture<Void> prepare() {
        return tx.prepare();
    }

    public ListenableFuture<Void> submit() {
        final ListenableFuture<Void> commit = tx.commit();
        tx = null;
        return commit;
    }

    public void closeForeignTransaction() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        if (tx != null) {
            tx.close();
            tx = null;
        }
    }
}
