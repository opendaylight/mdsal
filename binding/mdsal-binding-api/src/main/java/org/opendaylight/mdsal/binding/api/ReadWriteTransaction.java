/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.common.api.AsyncReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A transaction that enables combined read/write capabilities.
 *
 * <p>
 * For more information on usage and examples, please see the documentation in {@link AsyncReadWriteTransaction}.
 */
@Beta
public interface ReadWriteTransaction extends ReadTransaction, WriteTransaction,
        AsyncReadWriteTransaction<InstanceIdentifier<?>, DataObject> {
}
