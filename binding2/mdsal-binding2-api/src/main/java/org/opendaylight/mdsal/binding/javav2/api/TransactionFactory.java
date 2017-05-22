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
import org.opendaylight.mdsal.common.api.AsyncDataTransactionFactory;

/**
 * A factory which allocates new transactions to operate on the data tree.
 *
 * <p>
 * For more information on usage, please see the documentation in {@link AsyncDataTransactionFactory}.
 *
 * @see AsyncDataTransactionFactory
 */
@Beta
public interface TransactionFactory extends AsyncDataTransactionFactory<InstanceIdentifier<?>, TreeNode> {

    @Override
    ReadTransaction newReadOnlyTransaction();

    @Override
    WriteTransaction newWriteOnlyTransaction();
}
