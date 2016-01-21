/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;

class ForeignShardModificationContext {

    private final DOMDataTreeIdentifier identifier;
    private final DOMDataTreeShardProducer producer;
    private DOMDataTreeShardWriteTransaction tx;
    private DOMDataTreeWriteCursor cursor;

    ForeignShardModificationContext(final DOMDataTreeIdentifier identifier, final DOMDataTreeShardProducer producer) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.producer = Preconditions.checkNotNull(producer);
    }

    DOMDataTreeWriteCursor getCursor() {
        if (tx == null) {
            tx = producer.createTransaction();
        }
        if (cursor == null) {
            cursor = tx.createCursor(getIdentifier());
        }
        return cursor;
    }

    boolean isModified() {
        return tx != null;
    }

    void ready() {
        // FIXME: mark the fact this is ready, prevent instantiation of cursor
        if (cursor != null) {
            cursor.close();

            if (tx != null) {
                tx.ready();
            }
        }
    }

    DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }
}
