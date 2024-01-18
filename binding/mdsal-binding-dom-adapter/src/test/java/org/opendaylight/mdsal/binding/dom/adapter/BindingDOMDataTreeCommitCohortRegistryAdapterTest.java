/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.CommitCohortExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
public class BindingDOMDataTreeCommitCohortRegistryAdapterTest {
    @Mock
    private CommitCohortExtension cohortExtension;
    @Mock
    private Registration cohortRegistration;
    @Mock
    private DataTreeCommitCohort<Top> dataTreeCommitCohort;

    @Test
    void basicTest() {
        final var bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final var bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        doReturn(cohortRegistration).when(cohortExtension).registerCommitCohort(any(), any());
        doNothing().when(cohortRegistration).close();
        final var registryAdapter = new BindingDOMDataTreeCommitCohortRegistryAdapter(bindingTestContext.getCodec(),
            cohortExtension);

        final var dataTreeIdentifier = DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(Top.class));
        try (var reg = registryAdapter.registerCommitCohort(dataTreeIdentifier, dataTreeCommitCohort)) {
            // Nothing else
        }
    }
}