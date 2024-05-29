/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;

@ExtendWith(MockitoExtension.class)
class ReadFailedExceptionTest {
    @Mock
    private RpcError rpcError;

    @Test
    void readFailedExceptionTest() throws Exception {
        final var ex = new ReadFailedException("test", rpcError);
        assertEquals("test", ex.getMessage());
        assertEquals(List.of(rpcError), ex.getErrorList());
    }

    @Test
    void readFailedExceptionWithThrowableTest() throws Exception {
        final var npe = new NullPointerException();
        final var ex = new ReadFailedException("test", ReadFailedException.ofCaught(npe));
        assertEquals("test", ex.getMessage());
        final var errors = ex.getErrorList();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.getSeverity());
        assertEquals(ErrorType.APPLICATION, error.getErrorType());
        assertEquals(ErrorTag.OPERATION_FAILED, error.getTag());
        assertNull(error.getApplicationTag());
        assertNull(error.getInfo());

        final var cause = assertInstanceOf(ReadFailedException.class, ex.getCause());
        assertEquals("read encountered an unexpected failure", cause.getMessage());
        final var causeErrors = cause.getErrorList();
        assertEquals(1, causeErrors.size());
        final var causeError = causeErrors.get(0);
        assertEquals("read encountered an unexpected failure", causeError.getMessage());
        assertEquals(ErrorSeverity.ERROR, causeError.getSeverity());
        assertEquals(ErrorType.APPLICATION, causeError.getErrorType());
        assertEquals(ErrorTag.OPERATION_FAILED, causeError.getTag());
        assertNull(causeError.getApplicationTag());
        assertNull(causeError.getInfo());

        assertSame(npe, causeError.getCause());
    }
}