/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

// Non-final for mocking
class ShardRootModificationContext implements Identifiable<DOMDataTreeIdentifier> {

    private final DOMDataTreeIdentifier identifier;
    private final CursorAwareDataTreeSnapshot snapshot;

    private CursorAwareDataTreeModification modification = null;
    private DataTreeModificationCursorAdaptor cursor = null;

    ShardRootModificationContext(final DOMDataTreeIdentifier identifier,
            final CursorAwareDataTreeSnapshot snapshot) {
        this.identifier = requireNonNull(identifier);
        this.snapshot = requireNonNull(snapshot);
    }

    @Override
    public DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }

    DataTreeModificationCursorAdaptor cursor() {
        if (cursor == null) {
            if (modification == null) {
                modification = snapshot.newModification();
            }

            // FIXME: Should there be non-root path?
            DataTreeModificationCursor dataTreeCursor = modification.openCursor(YangInstanceIdentifier.empty()).get();
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

    void closeCursor() {
        cursor.close();
        cursor = null;
    }
}
