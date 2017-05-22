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
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;

@Beta
public interface ReadTransaction extends AsyncReadTransaction<InstanceIdentifier<?>, TreeNode> {

    <T extends TreeNode> void read(LogicalDatastoreType store, InstanceIdentifier<T> path,
       BiConsumer<LogicalDatastoreType, InstanceIdentifier<T>> callback) throws ReadFailedException;
}
