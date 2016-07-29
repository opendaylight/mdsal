/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.common.RpcError;


public class BasicExceptionTests {

    private static final RpcError RPC_ERROR = mock(RpcError.class);

    @Test(expected = TransactionCommitFailedException.class)
    public void transactionCommitFailedExceptionTest() throws Exception {
        throw new TransactionCommitFailedException("test", RPC_ERROR);
    }

    @Test(expected = TransactionCommitDeadlockException.class)
    public void transactionCommitDeadlockExceptionTest() throws Exception {
        throw new TransactionCommitDeadlockException(TransactionCommitDeadlockException.DEADLOCK_EXCEPTION_SUPPLIER
                .get().getMessage(), RPC_ERROR);
    }

    @Test(expected = TransactionChainClosedException.class)
    public void transactionChainClosedExceptionTest() throws Exception {
        throw new TransactionChainClosedException("test");
    }

    @Test(expected = TransactionChainClosedException.class)
    public void transactionChainClosedExceptionWithNullCauseTest() throws Exception {
        throw new TransactionChainClosedException("test", null);
    }

    @Test(expected = ReadFailedException.class)
    public void readFailedExceptionTest() throws Exception {
        throw new ReadFailedException("test", RPC_ERROR);
    }

    @Test(expected = ReadFailedException.class)
    public void readFailedExceptionWithThrowableTest() throws Exception {

        throw new ReadFailedException("test", ReadFailedException.MAPPER.apply(
                new NullPointerException()).getCause(), RPC_ERROR);
    }

    @Test(expected = OptimisticLockFailedException.class)
    public void optimisticLockFailedExceptionTest() throws Exception {
        throw new OptimisticLockFailedException("test");
    }

    @Test(expected = DataStoreUnavailableException.class)
    public void dataStoreUnavailableExceptionTest() throws Exception {
        throw new DataStoreUnavailableException("test", null);
    }

    @Test(expected = DataValidationFailedException.class)
    public void dataValidationFailedExceptionTest() throws Exception {
        final TestClass testClass = new TestClass();
        final DataValidationFailedException dataValidationFailedException =
                new DataValidationFailedException(TestClass.class, testClass, "test");

        assertEquals(testClass, dataValidationFailedException.getPath());
        assertEquals(TestClass.class, dataValidationFailedException.getPathType());

        throw dataValidationFailedException;
    }

    private final class TestClass implements Path {
        @Override
        public boolean contains(@Nonnull final Path other) {
            return false;
        }
    }
}