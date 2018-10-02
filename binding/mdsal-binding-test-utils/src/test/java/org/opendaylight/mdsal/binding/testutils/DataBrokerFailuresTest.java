/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testutils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

/**
 * Unit test for DataBrokerFailuresImpl.
 *
 * @author Michael Vorburger.ch
 */
@FixMethodOrder(NAME_ASCENDING)
public class DataBrokerFailuresTest {

    private final DataBrokerFailures dbFailures;
    private final DataBroker dataBroker;

    public DataBrokerFailuresTest() {
        DataBroker mockDataBroker = mock(DataBroker.class);
        ReadWriteTransaction readWriteTransaction = mock(ReadWriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(readWriteTransaction).commit();
        doReturn(readWriteTransaction).when(mockDataBroker).newReadWriteTransaction();
        WriteTransaction writeTransaction = mock(WriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();
        doReturn(writeTransaction).when(mockDataBroker).newWriteOnlyTransaction();
        dbFailures = new DataBrokerFailuresImpl(mockDataBroker);
        dataBroker = (DataBroker) dbFailures;
    }

    @Before
    public void setup() {

    }

    @Test
    public void testFailReadWriteTransactionCommit() throws TimeoutException, InterruptedException {
        dbFailures.failCommits(new OptimisticLockFailedException("bada boum bam!"));
        checkCommitFails();
        // Now make sure that it still fails, and not just once:
        checkCommitFails();
        // and still:
        checkCommitFails();
    }

    private void checkCommitFails() throws TimeoutException, InterruptedException {
        try {
            dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
            fail("This should have led to a TransactionCommitFailedException!");
        } catch (ExecutionException e) {
            assertTrue("Expected TransactionCommitFailedException",
                    e.getCause() instanceof TransactionCommitFailedException);
        }
    }

    @Test
    public void testFailReadWriteTransactionCommitNext()
            throws TimeoutException, InterruptedException, ExecutionException {
        // This must pass (the failCommits from previous test cannot affect this)
        // (It's a completely new instance of DataBroker & DataBrokerFailures anyways, but just to be to sure.)
        dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testFailTwoReadWriteTransactionCommit()
            throws TimeoutException, InterruptedException, ExecutionException {
        dbFailures.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
        checkCommitFails();
        // Now make sure that it still fails again a 2nd time, and not just once:
        checkCommitFails();
        // But now it should pass.. because we specified howManyTimes = 2 above
        dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
        dataBroker.newWriteOnlyTransaction().commit().get(5, TimeUnit.SECONDS);
        dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
    }

    @Test(expected = OptimisticLockFailedException.class)
    @SuppressWarnings("checkstyle:AvoidHidingCauseException")
    public void testFailWriteTransactionCommit()
            throws TimeoutException, InterruptedException, TransactionCommitFailedException {
        dbFailures.failCommits(new OptimisticLockFailedException("bada boum bam!"));
        try {
            dataBroker.newWriteOnlyTransaction().commit().get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertTrue("Expected TransactionCommitFailedException",
                    e.getCause() instanceof TransactionCommitFailedException);
            throw (TransactionCommitFailedException)e.getCause();
        }
    }

    @Test
    public void testUnfailCommits() throws TimeoutException, InterruptedException, ExecutionException {
        dbFailures.failCommits(new OptimisticLockFailedException("bada boum bam!"));
        checkCommitFails();
        dbFailures.unfailCommits();
        dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
        dataBroker.newWriteOnlyTransaction().commit().get(5, TimeUnit.SECONDS);
        dataBroker.newReadWriteTransaction().commit().get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testFailButCommitAnywayReadWriteTransaction() throws TimeoutException, InterruptedException {
        dbFailures.failButCommitAnyway();
        checkCommitFails();
    }

    // TODO make this work for TransactionChain as well ...

}
