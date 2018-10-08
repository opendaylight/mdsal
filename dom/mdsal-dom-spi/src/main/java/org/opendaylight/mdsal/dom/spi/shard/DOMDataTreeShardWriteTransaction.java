/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorProvider;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;

@Beta
public interface DOMDataTreeShardWriteTransaction extends DOMDataTreeCursorProvider {
    /**
     * Create a new write cursor. Any previous cursors have to be {@link DOMDataTreeWriteCursor#close()}d.
     *
     * @param prefix Tree identifier of the apex at which the cursor is rooted.
     * @return A new cursor rooted at specified prefx.
     * @throws IllegalStateException if a previous cursor has not been closed.
     * @throws NullPointerException if prefix is null.
     */
    @Override
    // FIXME: 4.0.0: reconcile @NonNull vs. super @Nullable
    @NonNull DOMDataTreeWriteCursor createCursor(@NonNull DOMDataTreeIdentifier prefix);

    /**
     * Finish this transaction and submit it for processing.
     *
     *<p>
     * FIXME: this method should accept a callback which will report success/failure. Let's not use a CheckedFuture
     *        due to overhead associated with attaching listeners to them.
     * @throws IllegalStateException if this transaction has an unclosed cursor.
     */
    void ready();

    /**
     * Close this transaction and all other foreign shard transactions that were opened as a part of this transaction.
     */
    void close();

    ListenableFuture<Void> submit();

    ListenableFuture<Boolean> validate();

    ListenableFuture<Void> prepare();

    ListenableFuture<Void> commit();

}
