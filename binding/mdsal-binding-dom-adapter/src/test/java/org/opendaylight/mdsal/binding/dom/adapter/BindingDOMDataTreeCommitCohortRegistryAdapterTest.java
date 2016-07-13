/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistration;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BindingDOMDataTreeCommitCohortRegistryAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final BindingBrokerTestFactory bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        final DOMDataTreeCommitCohortRegistry cohortRegistry = mock(DOMDataTreeCommitCohortRegistry.class);
        final DOMDataTreeCommitCohortRegistration cohortRegistration = mock(DOMDataTreeCommitCohortRegistration.class);
        doReturn(cohortRegistration).when(cohortRegistry)
                .registerCommitCohort(any(), any());
        doNothing().when(cohortRegistration).close();
        final BindingDOMDataTreeCommitCohortRegistryAdapter registryAdapter =
                new BindingDOMDataTreeCommitCohortRegistryAdapter(bindingTestContext.getCodec(), cohortRegistry);

        assertNotNull(registryAdapter.from(bindingTestContext.getCodec(), cohortRegistry));

        final DataTreeIdentifier dataTreeIdentifier = DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(Top.class));
        final DataTreeCommitCohort dataTreeCommitCohort = mock(DataTreeCommitCohort.class);
        final ObjectRegistration objectRegistration =
                registryAdapter.registerCommitCohort(dataTreeIdentifier, dataTreeCommitCohort);
        assertEquals(dataTreeCommitCohort, objectRegistration.getInstance());

        objectRegistration.close();
        verify(cohortRegistration).close();
    }
}