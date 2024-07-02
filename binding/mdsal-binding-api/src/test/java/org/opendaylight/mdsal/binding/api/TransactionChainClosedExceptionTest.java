/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class TransactionChainClosedExceptionTest {
    @Test
    void transactionChainClosedExceptionTest() {
        final var ex = new TransactionChainClosedException("test");
        assertEquals("test", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void transactionChainClosedExceptionWithNullCauseTest() {
        final var cause = new Throwable();
        final var ex = new TransactionChainClosedException("test", cause);
        assertEquals("test", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}