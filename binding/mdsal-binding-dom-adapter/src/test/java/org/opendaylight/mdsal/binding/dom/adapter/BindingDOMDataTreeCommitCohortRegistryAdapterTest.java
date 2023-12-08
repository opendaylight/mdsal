/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BindingDOMDataTreeCommitCohortRegistryAdapterTest {
    @Test
    void basicTest() {
        final var bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final var bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        final var cohortRegistry = mock(DOMDataTreeCommitCohortRegistry.class);
        final var cohortRegistration = mock(Registration.class);
        doReturn(cohortRegistration).when(cohortRegistry).registerCommitCohort(any(), any());
        doNothing().when(cohortRegistration).close();
        final var registryAdapter = new BindingDOMDataTreeCommitCohortRegistryAdapter(bindingTestContext.getCodec(),
            cohortRegistry);

        final var dataTreeIdentifier = DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(Top.class));
        final DataTreeCommitCohort<Top> dataTreeCommitCohort = mock(DataTreeCommitCohort.class);
        try (var objectRegistration = registryAdapter.registerCommitCohort(dataTreeIdentifier, dataTreeCommitCohort)) {
            assertSame(dataTreeCommitCohort, objectRegistration.getInstance());
        }
        verify(cohortRegistration).close();
    }
}