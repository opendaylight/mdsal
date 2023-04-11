/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;
import static org.opendaylight.mdsal.binding.util.Datastore.OPERATIONAL;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.binding.testutils.DataBrokerFailuresImpl;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link TransactionAdapter}.
 */
@Deprecated
public class TransactionAdapterTest extends AbstractConcurrentDataBrokerTest {

    private static final InstanceIdentifier<TopLevelList> TEST_PATH = path(TOP_FOO_KEY);

    private ManagedNewTransactionRunner managedNewTransactionRunner;
    private DataBrokerFailuresImpl testableDataBroker;

    private ManagedNewTransactionRunner createManagedNewTransactionRunnerToTest(final DataBroker dataBroker) {
        return new ManagedNewTransactionRunnerImpl(dataBroker);
    }

    @Before
    public void beforeTest() throws Exception {
        setup();
        testableDataBroker = new DataBrokerFailuresImpl(getDataBroker());
        managedNewTransactionRunner = createManagedNewTransactionRunnerToTest(testableDataBroker);
    }

    @Test
    public void testAdaptedWriteTransactionPutsSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            writeTx -> TransactionAdapter.toWriteTransaction(writeTx).put(LogicalDatastoreType.OPERATIONAL,
                    TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedReadWriteTransactionPutsSuccessfully() throws Exception {
        TopLevelList data = newTestDataObject();
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            writeTx -> TransactionAdapter.toReadWriteTransaction(writeTx).put(LogicalDatastoreType.OPERATIONAL,
                    TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedWriteTransactionFailsOnInvalidDatastore() throws Exception {
        Future<?> future = managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            writeTx -> TransactionAdapter.toWriteTransaction(writeTx).put(LogicalDatastoreType.CONFIGURATION,
                    TEST_PATH, newTestDataObject()));
        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get());
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
        assertEquals(Optional.empty(), syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedReadWriteTransactionFailsOnInvalidDatastore() throws Exception {
        Future<?> future = managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            writeTx -> TransactionAdapter.toReadWriteTransaction(writeTx).put(LogicalDatastoreType.CONFIGURATION,
                TEST_PATH, newTestDataObject()));
        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get());
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
        assertEquals(Optional.empty(), syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test(expected = ExecutionException.class)
    public void testAdaptedWriteTransactionCannotCommit() throws Exception {
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            tx -> TransactionAdapter.toWriteTransaction(tx).commit()).get();
    }

    @Test(expected = ExecutionException.class)
    public void testAdaptedReadWriteTransactionCannotCommit() throws Exception {
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            tx -> TransactionAdapter.toReadWriteTransaction(tx).commit()).get();
    }

    @Test(expected = ExecutionException.class)
    public void testAdaptedWriteTransactionCannotCancel() throws Exception {
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(OPERATIONAL,
            tx -> TransactionAdapter.toWriteTransaction(tx).cancel()).get();
    }

    @Test(expected = ExecutionException.class)
    public void testAdaptedReadWriteTransactionCannotCancel() throws Exception {
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(OPERATIONAL,
            tx -> TransactionAdapter.toReadWriteTransaction(tx).cancel()).get();
    }

    private static TopLevelList newTestDataObject() {
        TreeComplexUsesAugment fooAugment = new TreeComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build()).build();
        return topLevelList(TOP_FOO_KEY, fooAugment);
    }

    private <T extends DataObject> Optional<T> syncReadOptional(final LogicalDatastoreType datastoreType,
            final InstanceIdentifier<T> path) throws ExecutionException, InterruptedException {
        try (ReadTransaction tx = getDataBroker().newReadOnlyTransaction()) {
            return tx.read(datastoreType, path).get();
        }
    }

    private <T extends DataObject> T syncRead(final LogicalDatastoreType datastoreType,
            final InstanceIdentifier<T> path) throws ExecutionException, InterruptedException {
        return syncReadOptional(datastoreType, path).orElseThrow();
    }
}
