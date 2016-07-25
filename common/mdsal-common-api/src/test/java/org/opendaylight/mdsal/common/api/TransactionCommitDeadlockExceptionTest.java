/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.RpcError;

public class TransactionCommitDeadlockExceptionTest {

    @Test(expected = TransactionCommitDeadlockException.class)
    public void transactionCommitDeadlockExceptionTest() throws Exception {
        throw new TransactionCommitDeadlockException(TransactionCommitDeadlockException.DEADLOCK_EXCEPTION_SUPPLIER
                .get().getMessage(), mock(RpcError.class));
    }
}