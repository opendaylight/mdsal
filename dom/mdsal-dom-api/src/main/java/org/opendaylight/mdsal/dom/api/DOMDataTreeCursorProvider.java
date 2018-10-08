/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public interface DOMDataTreeCursorProvider {
    /**
     * Create a new {@link DataTreeModificationCursor} at specified path. May fail if specified path
     * does not exist.
     *
     * @param path Path at which the cursor is to be anchored
     * @return A new cursor, or null if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open, or the transaction
     *         is already closed (closed or submitted).
     */
    @Nullable DOMDataTreeCursor createCursor(@NonNull DOMDataTreeIdentifier path);
}
