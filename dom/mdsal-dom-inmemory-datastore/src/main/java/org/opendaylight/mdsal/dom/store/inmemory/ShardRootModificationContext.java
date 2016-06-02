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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

class ShardRootModificationContext {

    private final DOMDataTreeIdentifier identifier;
    private final CursorAwareDataTreeSnapshot snapshot;
    private CursorAwareDataTreeModification modification = null;
    private DataTreeModificationCursorAdaptor cursor = null;

    ShardRootModificationContext(final DOMDataTreeIdentifier identifier,
            final CursorAwareDataTreeSnapshot snapshot) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.snapshot = Preconditions.checkNotNull(snapshot);
    }

    public DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }

    DataTreeModificationCursorAdaptor cursor() {
        if (cursor == null || cursor.isClosed()) {
            if (modification == null) {
                modification = (CursorAwareDataTreeModification) snapshot.newModification();
            }

            // FIXME: Should there be non-root path?
            DataTreeModificationCursor dataTreeCursor =
                    modification.createCursor(YangInstanceIdentifier.EMPTY);
            cursor = DataTreeModificationCursorAdaptor.of(dataTreeCursor);
        }
        return cursor;
    }

    boolean isModified() {
        return modification != null;
    }

    DataTreeModification ready() {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        DataTreeModification ret = null;
        if (modification != null) {
            modification.ready();
            ret = modification;
            modification = null;
        }

        return ret;
    }
}
