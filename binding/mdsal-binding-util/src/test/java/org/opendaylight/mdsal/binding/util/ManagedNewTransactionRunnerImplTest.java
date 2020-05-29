/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.binding.util.Datastore.OPERATIONAL;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.binding.testutils.DataBrokerFailuresImpl;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link ManagedNewTransactionRunnerImpl}.
 *
 * @author Michael Vorburger.ch
 * @author Stephen Kitt
 */
public class ManagedNewTransactionRunnerImplTest extends AbstractConcurrentDataBrokerTest {

    static final InstanceIdentifier<TopLevelList> TEST_PATH = path(TOP_FOO_KEY);

    DataBrokerFailuresImpl testableDataBroker;
    ManagedNewTransactionRunner managedNewTransactionRunner;

    public ManagedNewTransactionRunnerImplTest() {
        super(true);
    }

    protected ManagedNewTransactionRunner createManagedNewTransactionRunnerToTest(DataBroker dataBroker) {
        return new ManagedNewTransactionRunnerImpl(dataBroker);
    }

    @Before
    public void beforeTest() throws Exception {
        setup();
        testableDataBroker = new DataBrokerFailuresImpl(getDataBroker());
        managedNewTransactionRunner = createManagedNewTransactionRunnerToTest(testableDataBroker);
    }

    @Test
    public void testApplyWithNewReadTransactionAndCloseEmptySuccessfully() {
        assertEquals(Long.valueOf(1),
            managedNewTransactionRunner.applyWithNewReadOnlyTransactionAndClose(OPERATIONAL, tx -> 1L));
    }

    @Test
    public void testCallWithNewReadTransactionAndCloseEmptySuccessfully() {
        managedNewTransactionRunner.callWithNewReadOnlyTransactionAndClose(OPERATIONAL, tx -> { });
    }

    @Test
    public void testCallWithNewTypedWriteOnlyTransactionAndSubmitEmptySuccessfully() throws Exception {
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL, writeTx -> { }).get();
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionAndSubmitEmptySuccessfully() throws Exception {
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL, tx -> { }).get();
    }

    @Test
    public void testApplyWithNewReadWriteTransactionAndSubmitEmptySuccessfully() throws Exception {
        assertEquals(1,
            (long) managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                tx -> 1).get());
    }

    @Test
    public void testCallWithNewTypedWriteOnlyTransactionAndSubmitPutSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            writeTx -> writeTx.put(TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionAndSubmitPutSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            tx -> tx.put(TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testApplyWithNewReadWriteTransactionAndSubmitPutSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        assertEquals(1, (long) managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(
            OPERATIONAL, tx -> {
                tx.put(TEST_PATH, data);
                return 1;
            }).get());
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testCallWithNewReadTransactionAndCloseReadSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            tx -> tx.put(TEST_PATH, data)).get();
        assertEquals(data, managedNewTransactionRunner.applyWithNewReadOnlyTransactionAndClose(OPERATIONAL,
            tx -> tx.read(TEST_PATH)).get().get());
    }

    TopLevelList newTestDataObject() {
        TreeComplexUsesAugment fooAugment = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build()).build();
        return topLevelList(TOP_FOO_KEY, fooAugment);
    }

    @Test
    public void testCallWithNewTypedWriteOnlyTransactionAndSubmitPutButLaterException() throws Exception {
        try {
            managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL, writeTx -> {
                writeTx.put(TEST_PATH, newTestDataObject());
                // We now throw an arbitrary kind of checked (not unchecked!) exception here
                throw new IOException("something didn't quite go as expected...");
            }).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionAndSubmitPutButLaterException() throws Exception {
        try {
            managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL, writeTx -> {
                writeTx.put(TEST_PATH, newTestDataObject());
                // We now throw an arbitrary kind of checked (not unchecked!) exception here
                throw new IOException("something didn't quite go as expected...");
            }).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testApplyWithNewReadWriteTransactionAndSubmitPutButLaterException() throws Exception {
        try {
            managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                writeTx -> {
                    writeTx.put(TEST_PATH, newTestDataObject());
                    // We now throw an arbitrary kind of checked (not unchecked!) exception here
                    throw new IOException("something didn't quite go as expected...");
                }).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testCallWithNewTypedWriteOnlyTransactionCommitFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(new TransactionCommitFailedException("bada boum bam!"));
            managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
                writeTx -> writeTx.put(TEST_PATH, newTestDataObject())).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionCommitFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(new TransactionCommitFailedException("bada boum bam!"));
            managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                writeTx -> writeTx.put(TEST_PATH, newTestDataObject())).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testApplyWithNewReadWriteTransactionCommitFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(new TransactionCommitFailedException("bada boum bam!"));
            managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                writeTx -> {
                    writeTx.put(TEST_PATH, newTestDataObject());
                    return 1;
                }).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testCallWithNewTypedWriteOnlyTransactionOptimisticLockFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
            managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
                writeTx -> writeTx.put(TEST_PATH, newTestDataObject())).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof OptimisticLockFailedException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testCallWithNewTypedReadWriteTransactionOptimisticLockFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
            managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                writeTx -> writeTx.put(TEST_PATH, newTestDataObject())).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof OptimisticLockFailedException);
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    @Test
    public void testApplyWithNewReadWriteTransactionOptimisticLockFailedException() throws Exception {
        try {
            testableDataBroker.failCommits(2, new OptimisticLockFailedException("bada boum bam!"));
            managedNewTransactionRunner.applyWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
                writeTx -> {
                    writeTx.put(TEST_PATH, newTestDataObject());
                    return 1;
                }).get();
            fail("This should have led to an ExecutionException!");
        } catch (ExecutionException e) {
            assertThat(e.getCause() instanceof OptimisticLockFailedException).isTrue();
        }
        assertThat(syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH)).isEmpty();
    }

    private <T extends DataObject> Optional<T> syncReadOptional(LogicalDatastoreType datastoreType,
            InstanceIdentifier<T> path) throws ExecutionException, InterruptedException {
        try (ReadTransaction tx = getDataBroker().newReadOnlyTransaction()) {
            return tx.read(datastoreType, path).get();
        }
    }

    <T extends DataObject> T syncRead(LogicalDatastoreType datastoreType, InstanceIdentifier<T> path)
            throws ExecutionException, InterruptedException {
        return syncReadOptional(datastoreType, path).get();
    }
}
