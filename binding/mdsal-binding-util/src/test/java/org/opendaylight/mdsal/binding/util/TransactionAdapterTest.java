/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.binding.testutils.DataBrokerFailuresImpl;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link TransactionAdapter}.
 */
@Deprecated
public class TransactionAdapterTest extends AbstractConcurrentDataBrokerTest {
    private static final InstanceIdentifier<TopLevelList> TEST_PATH = path(TOP_FOO_KEY);

    private ManagedNewTransactionRunner managedNewTransactionRunner;
    private DataBrokerFailuresImpl testableDataBroker;

    @Before
    public void beforeTest() throws Exception {
        setup();
        testableDataBroker = new DataBrokerFailuresImpl(getDataBroker());
        managedNewTransactionRunner = new ManagedNewTransactionRunnerImpl(testableDataBroker);
    }

    @Test
    public void testAdaptedWriteTransactionPutsSuccessfully() throws Exception {
        final var data = newTestDataObject();
        managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(Operational.VALUE,
            writeTx -> TransactionAdapter.toWriteTransaction(writeTx).put(LogicalDatastoreType.OPERATIONAL,
                    TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedReadWriteTransactionPutsSuccessfully() throws Exception {
        final var data = newTestDataObject();
        managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(Operational.VALUE,
            writeTx -> TransactionAdapter.toReadWriteTransaction(writeTx).put(LogicalDatastoreType.OPERATIONAL,
                    TEST_PATH, data)).get();
        assertEquals(data, syncRead(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedWriteTransactionFailsOnInvalidDatastore() throws Exception {
        final var future = managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(Operational.VALUE,
            writeTx -> TransactionAdapter.toWriteTransaction(writeTx).put(LogicalDatastoreType.CONFIGURATION,
                    TEST_PATH, newTestDataObject()));
        final var ex = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals(Optional.empty(), syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedReadWriteTransactionFailsOnInvalidDatastore() throws Exception {
        final var future = managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(Operational.VALUE,
            writeTx -> TransactionAdapter.toReadWriteTransaction(writeTx).put(LogicalDatastoreType.CONFIGURATION,
                TEST_PATH, newTestDataObject()));
        final var ex = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals(Optional.empty(), syncReadOptional(LogicalDatastoreType.OPERATIONAL, TEST_PATH));
    }

    @Test
    public void testAdaptedWriteTransactionCannotCommit() {
        final var future = managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(Operational.VALUE,
            tx -> TransactionAdapter.toWriteTransaction(tx).commit());

        final var ee = assertThrows(ExecutionException.class, future::get);
        final var cause = assertInstanceOf(UnsupportedOperationException.class, ee.getCause());
        assertEquals("Managed transactions must not be committed", cause.getMessage());
    }

    @Test
    public void testAdaptedReadWriteTransactionCannotCommit() {
        final var future = managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(Operational.VALUE,
            tx -> TransactionAdapter.toReadWriteTransaction(tx).commit());

        final var ee = assertThrows(ExecutionException.class, future::get);
        final var cause = assertInstanceOf(UnsupportedOperationException.class, ee.getCause());
        assertEquals("Managed transactions must not be committed", cause.getMessage());
    }

    @Test
    public void testAdaptedWriteTransactionCannotCancel() {
        final var future = managedNewTransactionRunner.callWithNewWriteOnlyTransactionAndSubmit(Operational.VALUE,
            tx -> TransactionAdapter.toWriteTransaction(tx).cancel());

        final var ee = assertThrows(ExecutionException.class, future::get);
        final var cause = assertInstanceOf(UnsupportedOperationException.class, ee.getCause());
        assertEquals("Managed transactions must not be cancelled", cause.getMessage());
    }

    @Test
    public void testAdaptedReadWriteTransactionCannotCancel() {
        final var future = managedNewTransactionRunner.callWithNewReadWriteTransactionAndSubmit(Operational.VALUE,
            tx -> TransactionAdapter.toReadWriteTransaction(tx).cancel());

        final var ee = assertThrows(ExecutionException.class, future::get);
        final var cause = assertInstanceOf(UnsupportedOperationException.class, ee.getCause());
        assertEquals("Managed transactions must not be cancelled", cause.getMessage());
    }

    private static TopLevelList newTestDataObject() {
        final var fooAugment = new TreeComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build()).build();
        return topLevelList(TOP_FOO_KEY, fooAugment);
    }

    private <T extends DataObject> Optional<T> syncReadOptional(final LogicalDatastoreType datastoreType,
            final InstanceIdentifier<T> path) throws ExecutionException, InterruptedException {
        try (var tx = getDataBroker().newReadOnlyTransaction()) {
            return tx.read(datastoreType, path).get();
        }
    }

    private <T extends DataObject> T syncRead(final LogicalDatastoreType datastoreType,
            final InstanceIdentifier<T> path) throws ExecutionException, InterruptedException {
        return syncReadOptional(datastoreType, path).orElseThrow();
    }
}
