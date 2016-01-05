/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

class ReadableCursorDelegatingOperation implements ReadableCursorOperation {

    private final DataTreeSnapshotCursor cursor;

    protected ReadableCursorDelegatingOperation(DataTreeSnapshotCursor cursor) {
        this.cursor = Preconditions.checkNotNull(cursor, "cursor");
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(PathArgument arg) {
        return cursor.readNode(arg);
    }

    @Override
    public void exit() {
        cursor.exit();
    }

    @Override
    public ReadableCursorOperation enter(PathArgument arg) {
        cursor.enter(arg);
        return this;
    }

}