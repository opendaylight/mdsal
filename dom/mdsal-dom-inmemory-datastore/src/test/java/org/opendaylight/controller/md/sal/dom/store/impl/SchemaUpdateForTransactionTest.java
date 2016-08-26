/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.md.sal.dom.store.impl;

import static org.junit.Assert.assertNotNull;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class SchemaUpdateForTransactionTest {

    private static final YangInstanceIdentifier TOP_PATH = YangInstanceIdentifier.of(TestModel.TEST_QNAME);
    private SchemaContext schemaContext;
    private InMemoryDOMDataStore domStore;

    @Before
    public void setupStore() {
        domStore = new InMemoryDOMDataStore("TEST", MoreExecutors.newDirectExecutorService());
        // loadSchemas(RockTheHouseInput.class);
    }

    public void loadSchemas(final Class<?>... classes) {
        // YangModuleInfo moduleInfo;
        // try {
        // ModuleInfoBackedContext context = ModuleInfoBackedContext.create();
        // for (Class<?> clz : classes) {
        // moduleInfo = BindingReflections.getModuleInfo(clz);
        //
        // context.registerModuleInfo(moduleInfo);
        // }
        // schemaContext = context.tryToCreateSchemaContext().get();
        // domStore.onGlobalContextUpdated(schemaContext);
        // } catch (Exception e) {
        // Throwables.propagateIfPossible(e);
        // }
    }

    /**
     * Test suite tests allocating transaction when schema context
     * does not contain module necessary for client write,
     * then triggering update of global schema context
     * and then performing write (according to new module).
     *
     *<p>
     * If transaction between allocation and schema context was
     * unmodified, it is safe to change its schema context
     * to new one (e.g. it will be same as if allocated after
     * schema context update.)
     *
     *<p>
     * @throws InterruptedException when this condition met
     * @throws ExecutionException when execution fails
     */
    @Ignore
    @Test
    public void testTransactionSchemaUpdate() throws InterruptedException, ExecutionException {
        // FIXME: Rewrite this test to be pure DOM only.
        assertNotNull(domStore);

        // We allocate transaction, initial schema context does not
        // contain Lists model
        final DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        // we trigger schema context update to contain Lists model
        // loadSchemas(RockTheHouseInput.class, Top.class);

        /**
         *
         * Writes /test in writeTx, this write should not fail
         * with IllegalArgumentException since /test is in
         * schema context.
         *
         */
        // writeTx.write(TOP_PATH, ImmutableNodes.containerNode(Top.QNAME));

    }

}
