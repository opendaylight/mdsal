/*
 * Copyright (c) 2018 Pantheon Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.TransactionDatastoreMismatchException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface DOMDataTreeWriteOperations {
    /**
     * Stores a piece of data at the specified path. This acts as an add / replace operation, which is to say that whole
     * subtree will be replaced by the specified data.
     *
     * <p>
     * If you need to make sure that a parent object exists but you do not want modify its pre-existing state by using
     * put, consider using {@link #merge} instead.
     *
     * @param path the data object path
     * @param data the data object to be written to the specified path
     * @throws IllegalArgumentException if {@code store} is not supported
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any argument is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    void put(YangInstanceIdentifier path, NormalizedNode data);

    /**
     * Merges a piece of data with the existing data at a specified path. Any pre-existing data which is not explicitly
     * overwritten will be preserved. This means that if you store a container, its child lists will be merged.
     *
     * <p>
     * If you require an explicit replace operation, use {@link #put} instead.
     *
     * @param path the data object path
     * @param data the data object to be merged to the specified path
     * @throws IllegalArgumentException if {@code store} is not supported
     * @throws IllegalStateException if the transaction has already been submitted
     * @throws NullPointerException if any argument is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    void merge(YangInstanceIdentifier path, NormalizedNode data);

    /**
     * Removes a piece of data from specified path. This operation does not fail if the specified path does not exist.
     *
     * @param path Data object path
     * @throws IllegalArgumentException if {@code store} is not supported
     * @throws IllegalStateException if the transaction was committed or canceled.
     * @throws NullPointerException if any argument is {@code null}
     * @throws TransactionDatastoreMismatchException if this transaction is already bound to a different data store
     */
    void delete(YangInstanceIdentifier path);

    /**
     * Return a {@link FluentFuture} which completes.
     *
     * @return A future which completes when the requested operations complete.
     */
    @Beta
    @CheckReturnValue
    @NonNull FluentFuture<?> completionFuture();
}
