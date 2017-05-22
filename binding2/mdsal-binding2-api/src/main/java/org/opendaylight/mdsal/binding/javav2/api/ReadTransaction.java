/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.function.BiConsumer;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A transaction that provides a stateful read-only view of the data tree.
 *
 * <p>
 * For more information on usage and examples, please see the documentation in
 *  {@link org.opendaylight.mdsal.common.api.AsyncReadTransaction}.
 */
@Beta
public interface ReadTransaction extends AsyncReadTransaction<InstanceIdentifier<?>, TreeNode> {

    /**
     * Reads data from the provided logical data store located at the provided path.
     *
     *<p>
     * If the target is a subtree, then the whole subtree is read (and will be
     * accessible from the returned data object).
     *
     * @param store
     *            Logical data store from which read should occur.
     * @param path
     *            Path which uniquely identifies subtree which client want to
     *            read
     * @param callback result callback
     * @param <T> result type
     */
    <T extends TreeNode> void read(LogicalDatastoreType store, InstanceIdentifier<T> path,
        BiConsumer<ReadFailedException, T> callback);

    /**
     * Note: Existing binding-dom v1 compatibility method.
     * Reads data from the provided logical data store located at the provided path.
     *
     *<p>
     * If the target is a subtree, then the whole subtree is read (and will be
     * accessible from the returned data object).
     *
     * @param store
     *            Logical data store from which read should occur.
     * @param path
     *            Path which uniquely identifies subtree which client want to
     *            read
     * @param <T> data tree type
     * @return a CheckFuture containing the result of the read. The Future blocks until the
     *         commit operation is complete. Once complete:
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns an Optional object
     *         containing the data.</li>
     *         <li>If the data at the supplied path does not exist, the Future returns
     *         Optional#absent().</li>
     *         <li>If the read of the data fails, the Future will fail with a
     *         {@link ReadFailedException} or an exception derived from ReadFailedException.</li>
     *         </ul>
     */
    <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> read(
            LogicalDatastoreType store, org.opendaylight.yangtools.yang.binding.InstanceIdentifier<T> path);
}
