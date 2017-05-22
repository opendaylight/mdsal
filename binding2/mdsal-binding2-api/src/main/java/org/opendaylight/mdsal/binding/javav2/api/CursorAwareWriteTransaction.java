/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

/**
 * Write transaction that provides cursor's with write access to the data tree.
 */
@Beta
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
    @Nullable
    @Override
    <T extends TreeNode> DataTreeCursor createCursor(@Nonnull DataTreeIdentifier<T> path);

    /**
     * Cancels the transaction.
     *
     * <p>
     * Transactions can only be cancelled if it was not yet submited.
     *
     * <p>
     * Invoking cancel() on failed or already canceled will have no effect, and transaction is
     * considered cancelled.
     *
     * <p>
     * Invoking cancel() on finished transaction (future returned by {@link #submit(BiConsumer)} already
     * successfully completed) will always fail (return false).
     *
     * @return <tt>false</tt> if the task could not be cancelled, typically because it has already
     *         completed normally; <tt>true</tt> otherwise
     *
     */
    boolean cancel();

    /**
     * Submits this transaction to be asynchronously applied to update the logical data tree. Callback
     * conveys the result of applying the data changes.
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
     * @param callback result callback
     * @param <T> result type
     */
    <T extends TreeNode> void submit(BiConsumer<TransactionCommitFailedException, T> callback);
}