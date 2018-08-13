/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.mdsal.common.api.AsyncDataTransactionFactory;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface TransactionFactory extends AsyncDataTransactionFactory<InstanceIdentifier<?>, DataObject> {
    @Override
    ReadTransaction newReadOnlyTransaction();

    @Override
    ReadWriteTransaction newReadWriteTransaction();

    @Override
    WriteTransaction newWriteOnlyTransaction();
}
