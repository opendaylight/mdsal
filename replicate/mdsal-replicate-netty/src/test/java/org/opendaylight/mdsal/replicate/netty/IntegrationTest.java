/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.net.Inet4Address;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.dom.impl.DOMClusterSingletonServiceProviderImpl;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IntegrationTest extends AbstractDataBrokerTest {
    private static final int TEST_PORT = 4000;

    private AbstractBootstrapSupport support;
    private DOMClusterSingletonServiceProviderImpl css;

    @Before
    public void before() {
        support = AbstractBootstrapSupport.create();
        css = new DOMClusterSingletonServiceProviderImpl(new SimpleDOMEntityOwnershipService());
        css.initializeProvider();
    }

    @After
    public void after() throws InterruptedException {
        support.close();
        css.close();
    }

    @Test
    public void testSourceToSink() throws InterruptedException {
        // Make sure to start source...
        final Registration source = NettyReplication.createSource(support, getDomBroker(), css, true, TEST_PORT);
        // ... and give it some time start up and open up the port
        Thread.sleep(1000);

        // Mocking for sink...
        final DOMTransactionChain sinkChain = mock(DOMTransactionChain.class);
        final DOMDataTreeWriteTransaction sinkTx = mock(DOMDataTreeWriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(sinkTx).commit();
        doReturn(sinkTx).when(sinkChain).newWriteOnlyTransaction();
        final DOMDataBroker sinkBroker = mock(DOMDataBroker.class);
        doReturn(sinkChain).when(sinkBroker).createMergingTransactionChain(any());

        // Kick of the sink ...
        final Registration sink = NettyReplication.createSink(support, sinkBroker, css, true,
            Inet4Address.getLoopbackAddress(), TEST_PORT, Duration.ZERO);
        // ... and sync on it starting up
        verify(sinkBroker, timeout(1000)).createMergingTransactionChain(any());

        // FIXME: add a few writes to the broker so we have multiple transactions and verify deltas

        verify(sinkChain, timeout(2000)).newWriteOnlyTransaction();
        verify(sinkTx, timeout(1000)).put(eq(LogicalDatastoreType.CONFIGURATION), eq(YangInstanceIdentifier.empty()),
            any(ContainerNode.class));
        verify(sinkTx, timeout(1000)).commit();

        sink.close();
        source.close();
    }
}
