/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.mdsal.common.api.AsyncReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * A transaction that provides a stateful read-only view of the data tree.
 * <p>
 * For more information on usage and examples, please see the documentation in
 *  {@link org.opendaylight.mdsal.common.api.AsyncReadTransaction}.
 */
public interface ReadTransaction extends AsyncReadTransaction<InstanceIdentifier<?>, DataObject> {

    /**
     * Reads data from the provided logical data store located at the provided path.
     *<p>
     * If the target is a subtree, then the whole subtree is read (and will be
     * accessible from the returned data object).
     *
     * @param store
     *            Logical data store from which read should occur.
     * @param path
     *            Path which uniquely identifies subtree which client want to
     *            read
     * @return a CheckFuture containing the result of the read. The Future blocks until the
     *         commit operation is complete. Once complete:
     *         &lt;ul&gt;
     *         &lt;li&gt;If the data at the supplied path exists, the Future returns an Optional object
     *         containing the data.&lt;/li&gt;
     *         &lt;li&gt;If the data at the supplied path does not exist, the Future returns
     *         Optional#absent().&lt;/li&gt;
     *         &lt;li&gt;If the read of the data fails, the Future will fail with a
     *         {@link ReadFailedException} or an exception derived from ReadFailedException.&lt;/li&gt;
     *         &lt;/ul&gt;
     */
    <T extends DataObject> CheckedFuture<Optional<T>,ReadFailedException> read(
            LogicalDatastoreType store, InstanceIdentifier<T> path);
}
