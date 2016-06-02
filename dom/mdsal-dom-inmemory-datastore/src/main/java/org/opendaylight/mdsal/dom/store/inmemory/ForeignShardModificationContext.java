/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
final class ForeignShardModificationContext {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignShardModificationContext.class);
    private final DOMDataTreeIdentifier identifier;
    private final DOMDataTreeShardProducer producer;
    private DOMDataTreeShardWriteTransaction tx;
    private DOMDataTreeWriteCursor cursor;
    private volatile boolean notReady = true;

    ForeignShardModificationContext(final DOMDataTreeIdentifier identifier, final DOMDataTreeShardProducer producer) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.producer = Preconditions.checkNotNull(producer);
    }

    DOMDataTreeWriteCursor getCursor() {
        Preconditions.checkState(notReady, "Context %s has been readied", this);

        if (cursor == null) {
            if (tx == null) {
                tx = producer.createTransaction();
            }
            cursor = tx.createCursor(getIdentifier());
        }
        return cursor;
    }

    boolean isModified() {
        return tx != null;
    }

    void ready() {
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

    DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }

    ListenableFuture<Boolean> validate() {
        return tx.validate();
    }

    ListenableFuture<Void> prepare() {
        return tx.prepare();
    }

    ListenableFuture<Void> submit() {
        final ListenableFuture<Void> commit = tx.commit();
        tx = null;
        return commit;
    }
}
