/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.RpcError;

@ExtendWith(MockitoExtension.class)
class TransactionCommitFailedExceptionTest {
    @Mock
    private RpcError error;

    @Test
    void transactionCommitFailedExceptionTest() {
        final var ex = new TransactionCommitFailedException("test", error);
        assertEquals("test", ex.getMessage());
        assertNull(ex.getCause());
        assertEquals(List.of(error), ex.getErrorList());
    }
}