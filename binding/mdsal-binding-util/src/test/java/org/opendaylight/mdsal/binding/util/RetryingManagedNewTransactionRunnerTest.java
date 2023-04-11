/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.mdsal.binding.util.Datastore.OPERATIONAL;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;

/**
 * Test for {@link RetryingManagedNewTransactionRunner}.
 * Note that this test (intentionally) extends the {@link ManagedNewTransactionRunnerImplTest}.
 *
 * @author Michael Vorburger.ch
 */
public class RetryingManagedNewTransactionRunnerTest extends ManagedNewTransactionRunnerImplTest {

    @Override
    protected ManagedNewTransactionRunner createManagedNewTransactionRunnerToTest(final DataBroker dataBroker) {
        return new RetryingManagedNewTransactionRunner(dataBroker);
    }

    @Override
    public void testCallWithNewTypedWriteOnlyTransactionOptimisticLockFailedException() throws Exception {
        // contrary to the super() test implementation for (just) ManagedNewTransactionRunnerImpl, in the parent class
        // here we expect the x2 OptimisticLockFailedException to be retried, and then eventually succeed:
        testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            writeTx -> writeTx.put(TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Override
    public void testCallWithNewTypedReadWriteTransactionOptimisticLockFailedException() throws Exception {
        // contrary to the super() test implementation for (just) ManagedNewTransactionRunnerImpl, in the parent class
        // here we expect the x2 OptimisticLockFailedException to be retried, and then eventually succeed:
        testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            writeTx -> writeTx.put(TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Override
    public void testApplyWithNewReadWriteTransactionOptimisticLockFailedException() throws Exception {
        // contrary to the super() test implementation for (just) ManagedNewTransactionRunnerImpl, in the parent class
        // here we expect the x2 OptimisticLockFailedException to be retried, and then eventually succeed:
        testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
        TopLevelList data = newTestDataObject();
        assertEquals(1, (long) managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(
            OPERATIONAL, writeTx -> {
                writeTx.put(TEST_PATH, data);
                return 1;
            }).get());
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionReadFailedException() throws Exception {
        testableDataBroker.failReads(2, new ReadFailedException("bada boum bam!"));
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            tx -> {
                tx.put(TEST_PATH, data);
                assertEquals(Optional.of(data), tx.read(TEST_PATH).get());
            }).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testApplyWithNewReadWriteTransactionReadFailedException() throws Exception {
        testableDataBroker.failReads(2, new ReadFailedException("bada boum bam!"));
        TopLevelList data = newTestDataObject();
        assertEquals(data, managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(
            OPERATIONAL,
            tx -> {
                tx.put(TEST_PATH, data);
                return tx.read(TEST_PATH).get().orElseThrow();
            }).get());
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }
}
