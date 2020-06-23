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

import com.google.common.collect.ImmutableClassToInstanceMap;
import java.net.Inet4Address;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.dom.impl.DOMClusterSingletonServiceProviderImpl;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IntegrationTest extends AbstractDataBrokerTest {
    private static final int TEST_PORT = 4000;
    private static final QName BASE_QNAME = QName.create(
        "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test:cars", "2014-03-13", "cars");

    private static final QName CARS_QNAME = QName.create(BASE_QNAME, "cars");
    private static final QName CAR_QNAME = QName.create(CARS_QNAME, "car");
    private static final QName CAR_NAME_QNAME = QName.create(CAR_QNAME, "name");

    private static final YangInstanceIdentifier BASE_PATH = YangInstanceIdentifier.of(BASE_QNAME);
    private static final YangInstanceIdentifier CAR_LIST_PATH = BASE_PATH.node(CAR_QNAME);

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

    @Test
    public void testSendingChangeSourceToSink() throws InterruptedException {
        // Make sure to start source...
        final DOMDataBroker domBroker =  mock(DOMDataBroker.class);
        final DOMDataTreeChangeService dtcs =  mock(DOMDataTreeChangeService.class);
        doReturn(ImmutableClassToInstanceMap.of(DOMDataTreeChangeService.class, dtcs)).when(domBroker).getExtensions();

        final Registration source = NettyReplication.createSource(support, domBroker, css, true, TEST_PORT);
        Thread.sleep(1000);
//         Mocking for sink...
        final DOMTransactionChain sinkChain = mock(DOMTransactionChain.class);
        final DOMDataTreeWriteTransaction sinkTx = mock(DOMDataTreeWriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(sinkTx).commit();
        doReturn(sinkTx).when(sinkChain).newWriteOnlyTransaction();
        final DOMDataBroker sinkBroker = mock(DOMDataBroker.class);
        doReturn(sinkChain).when(sinkBroker).createMergingTransactionChain(any());

        // Kick of the sink ...
        final Registration sink = NettyReplication.createSink(support, sinkBroker, css, true,
            Inet4Address.getLoopbackAddress(), TEST_PORT, Duration.ZERO);

        verify(sinkBroker, timeout(1000)).createMergingTransactionChain(any());
        // Capture the newly created and registered ClusteredDOMDataTreeChangeListener
        final ArgumentCaptor<ClusteredDOMDataTreeChangeListener> listenerCaptor =
            ArgumentCaptor.forClass(ClusteredDOMDataTreeChangeListener.class);
        verify(dtcs, timeout(1000)).registerDataTreeChangeListener(any(), listenerCaptor.capture());
        // call the onDataTreeChanged() with some candidates and verify that the sink received and applied them

        final ClusteredDOMDataTreeChangeListener listener = listenerCaptor.getAllValues().iterator().next();
        final int dtcCount = 3;
        listener.onDataTreeChanged(createChange(dtcCount));
        verify(sinkChain, timeout(2000).times(dtcCount)).newWriteOnlyTransaction();

        sink.close();
        source.close();
    }

    private Collection<DataTreeCandidate> createChange(final int candidateCount) {
        final DataTreeCandidate candidate = DataTreeCandidates.fromNormalizedNode(CAR_LIST_PATH,
            Builders.mapBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(CAR_QNAME))
                .withChild(createCar())
                .build());
        return Collections.nCopies(candidateCount, candidate);
    }

    private MapEntryNode createCar() {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(YangInstanceIdentifier.NodeIdentifierWithPredicates
                .of(CAR_QNAME,CAR_NAME_QNAME, "name"))
            .withChild(ImmutableNodes.leafNode(CAR_NAME_QNAME, "name"))
            .build();
    }
}
