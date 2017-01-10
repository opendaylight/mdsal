/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.mdsal.common.api.TransactionChain;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A chain of transactions.
 *
 * <p>
 * For more information about transaction chaining and transaction chains
 * see {@link TransactionChain}.
 *
 * @see TransactionChain
 *
 */
public interface BindingTransactionChain extends TransactionFactory,
        TransactionChain<InstanceIdentifier<?>, DataObject> {

    @Override
    ReadTransaction newReadOnlyTransaction();

    @Override
    WriteTransaction newWriteOnlyTransaction();

    @Override
    ReadWriteTransaction newReadWriteTransaction();
}
