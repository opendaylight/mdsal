/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.TOP_FOO_KEY;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.path;
import static org.opendaylight.mdsal.binding.test.model.util.ListsBindingUtils.topLevelList;

import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Integration tests the AbstractDataBrokerTest.
 *
 * @author Michael Vorburger
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractDataBrokerTestTest extends AbstractConcurrentDataBrokerTest {
    private static final InstanceIdentifier<Top> LEGACY_TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final org.opendaylight.mdsal.binding.api.InstanceIdentifier<Top> TOP_PATH =
            org.opendaylight.mdsal.binding.api.InstanceIdentifier.create(Top.class);

    @Before
    public void before() {
        assertNotNull(getDataBroker());
    }

    @Test
    public void aEnsureDataBrokerIsNotNull() {
        assertNotNull(getDataBroker());
    }

    @Test
    public void bPutSomethingIntoDataStore() throws Exception {
        writeInitialState();
        assertTrue(isTopInDataStore());
    }

    @Test
    public void cEnsureDataStoreIsEmptyAgainInNewTest() throws Exception {
        assertFalse(isTopInDataStore());
    }

    // copy/pasted from Bug1125RegressionTest.writeInitialState()
    private void writeInitialState() throws InterruptedException, ExecutionException {
        WriteTransaction initialTx = getDataBroker().newWriteOnlyTransaction();
        initialTx.put(LogicalDatastoreType.OPERATIONAL, LEGACY_TOP_PATH, new TopBuilder().build());
        TreeComplexUsesAugment fooAugment = new TreeComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build()).build();
        initialTx.put(LogicalDatastoreType.OPERATIONAL, path(TOP_FOO_KEY), topLevelList(TOP_FOO_KEY, fooAugment));
        initialTx.commit().get();
    }

    private boolean isTopInDataStore() throws InterruptedException, ExecutionException {
        try (ReadTransaction readTx = getDataBroker().newReadOnlyTransaction()) {
            return readTx.exists(LogicalDatastoreType.OPERATIONAL, LEGACY_TOP_PATH).get()
                    && readTx.exists(LogicalDatastoreType.OPERATIONAL, TOP_PATH).get();
        }
    }
}
