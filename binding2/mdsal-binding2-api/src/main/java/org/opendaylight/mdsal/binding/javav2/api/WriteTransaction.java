/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * A transaction that provides mutation capabilities on a data tree.
 *
 * <p>
 * For more information on usage and examples, please see the documentation in {@link AsyncWriteTransaction}.
 */
@Beta
public interface WriteTransaction extends AsyncWriteTransaction<InstanceIdentifier<?>, TreeNode> {

    <T extends TreeNode> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data);

    <T extends TreeNode> void put(LogicalDatastoreType store, InstanceIdentifier<T> path, T data, boolean createMissingParents);

    <T extends TreeNode> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data);

    <T extends TreeNode> void merge(LogicalDatastoreType store, InstanceIdentifier<T> path, T data,
        boolean createMissingParents);

    @Override
    void delete(LogicalDatastoreType store, InstanceIdentifier<? extends TreeNode> path);
}
