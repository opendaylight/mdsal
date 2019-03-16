/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.FluentFuture;
import javax.annotation.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Write transaction that provides cursor's with write access to the data tree.
 */
public interface CursorAwareWriteTransaction extends DataTreeCursorProvider {
    /**
     * Create a {@link DataTreeWriteCursor} anchored at the specified path.
     * There can only be one cursor open at a time.
     *
     * <p>
     * @param path Path at which the cursor is to be anchored
     * @return write cursor at the desired location.
     * @throws IllegalStateException when there's an open cursor, or this transaction is closed already.
     */
    @Override
    <T extends DataObject> DataTreeWriteCursor createCursor(DataTreeIdentifier<T> path);

    /**
     * Cancels the transaction.
     *
     * <p>
     * A transaction can only be cancelled if it has not yet been committed.
     *
     * <p>
     * Invoking cancel() on failed or already canceled will have no effect, and transaction is
     * considered cancelled.
     *
     * <p>
     * Invoking cancel() on finished transaction (future returned by {@link #commit()} already
     * successfully completed) will always fail (return false).
     *
     * @return {@code false} if the task could not be cancelled, typically because it has already
     *         completed normally; {@code true} otherwise
     */
    boolean cancel();

    /**
     * Commits this transaction to be asynchronously applied to update the logical data tree. The
     * returned CheckedFuture conveys the result of applying the data changes.
     *
     * <p>
     * <b>Note:</b> It is strongly recommended to process the FluentFuture result in an
     * asynchronous manner rather than using the blocking get() method.
     *
     * <p>
     * This call logically seals the transaction, which prevents the client from further changing
     * data tree using this transaction's cursor. Any subsequent calls to
     * <code>createCursorCursor(DOMDataTreeIdentifier</code>
     * or any of the cursor's methods will fail with {@link IllegalStateException}.
     *
     * <p>
     * The transaction is marked as submitted and enqueued into the shard back-end for
     * processing.
     *
     * @return a FluentFuture containing the result of the commit information. The Future blocks until the commit
     *         operation is complete. A successful commit returns nothing. On failure, the Future will fail with a
     *         {@link TransactionCommitFailedException} or an exception derived from TransactionCommitFailedException.
     */
    @CheckReturnValue
    @NonNull FluentFuture<? extends @NonNull CommitInfo> commit();
}
