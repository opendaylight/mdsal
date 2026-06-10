/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.mdsal.singleton.impl.EOSClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.EntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.EntityKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IntegrationTest extends AbstractDataBrokerTest {
    private static final int TEST_PORT = 4000;

    private static final QName ENTITY_QNAME = Entity.QNAME;
    private static final QName ENTITY_NAME_QNAME = QName.create(ENTITY_QNAME, "name");

    private AbstractBootstrapSupport support;
    private EOSClusterSingletonServiceProvider css;

    @Before
    public void before() {
        support = AbstractBootstrapSupport.create();
        css = new EOSClusterSingletonServiceProvider(new SimpleDOMEntityOwnershipService());
    }

    @After
    public void after() throws Exception {
        support.close();
        css.close();
    }

    /**
     * Connect the Source and Sink. Verify that the initial state returned from Source was empty. After that make some
     * changes to the Source's datastore and verify that they were all replicated to the Sink
     */
    @Test
    public void testSourceToSink() throws InterruptedException, ExecutionException, UnknownHostException {
        // Make sure to start source...
        try (var source = NettyReplicationSource.createSource(support, getDomBroker(), css, true, TEST_PORT,
            Duration.ZERO, 5)) {
            // ... and give it some time start up and open up the port
            Thread.sleep(1000);

            // Mocking for sink...
            final DOMTransactionChain sinkChain = mock(DOMTransactionChain.class);
            final DOMDataTreeWriteTransaction sinkTx = mock(DOMDataTreeWriteTransaction.class);
            doReturn(CommitInfo.emptyFluentFuture()).when(sinkTx).commit();
            doCallRealMethod().when(sinkTx).commit(any(), any());
            doReturn(sinkTx).when(sinkChain).newWriteOnlyTransaction();
            final DOMDataBroker sinkBroker = mock(DOMDataBroker.class);
            doReturn(sinkChain).when(sinkBroker).createMergingTransactionChain();
            //Creating the tree for config
            DOMDataTreeIdentifier confTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                    YangInstanceIdentifier.of());

            //Creating the tree for operational
            DOMDataTreeIdentifier operTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
                    YangInstanceIdentifier.of());

            NettyReplicationConfig config = new NettyReplicationConfig(support,
                    sinkBroker,
                    new InetSocketAddress(Inet4Address.getLoopbackAddress(), TEST_PORT),
                    Duration.ZERO,
                    Duration.ZERO,
                    3,
                    List.of(confTree, operTree));

            // Kick of the sink ...
            try (var sink = NettyReplicationSink.createSinkReg(css, config)) {
                // ... and sync on it starting up

                // verify the connection was established and MSG_EMPTY_DATA was transferred
                // now two times because we are doing for both configuration and operational data trees.
                verify(sinkBroker, timeout(1000).times(2)).createMergingTransactionChain();
                verify(sinkTx, timeout(1000)).put(eq(LogicalDatastoreType.CONFIGURATION),
                    eq(YangInstanceIdentifier.of()), any(ContainerNode.class));
                verify(sinkTx, timeout(1000)).put(eq(LogicalDatastoreType.OPERATIONAL),
                        eq(YangInstanceIdentifier.of()), any(ContainerNode.class));

                // generate some deltas
                final int deltaCount = 5;
                generateModification(getDataBroker(), deltaCount);

                // verify that all the deltas were transferred and committed + 1 invocation from receiving
                // MSG_EMPTY_DATA
                // now delta+1 times two times because we are doing for both configuration and operational data trees.
                verify(sinkChain, timeout(2000).times(2 * (deltaCount + 1))).newWriteOnlyTransaction();
                verify(sinkTx, timeout(2000).times(2 * (deltaCount + 1))).commit(any(), any());
            }
        }
    }

    /**
     * Add some data first and then start replication. One initial change is expected per subscribed
     * datastore tree, and each tree should contain the expected initial state from the source.
     */
    @Test
    public void testReplicateInitialState() throws InterruptedException, ExecutionException {
        // add some data to datastore
        final int deltaCount = 5;
        generateModification(getDataBroker(), deltaCount);

        // expected initial state per tree
        final NormalizedNode expectedConfigState = generateConfiguationNormalizedNodeForEntities(deltaCount);
        final NormalizedNode expectedOperationalState = generateOperationalNormalizedNodeForEntities(deltaCount);

        // Make sure to start source...
        try (var source = NettyReplicationSource.createSource(
                support, getDomBroker(), css, true, TEST_PORT, Duration.ZERO, 5)) {
            // ... and give it some time start up and open up the port
            Thread.sleep(1000);

            // Mocking for sink...
            final DOMTransactionChain sinkChain = mock(DOMTransactionChain.class);
            final DOMDataTreeWriteTransaction sinkTx = mock(DOMDataTreeWriteTransaction.class);
            doReturn(CommitInfo.emptyFluentFuture()).when(sinkTx).commit();
            doCallRealMethod().when(sinkTx).commit(any(), any());
            doReturn(sinkTx).when(sinkChain).newWriteOnlyTransaction();

            final DOMDataBroker sinkBroker = mock(DOMDataBroker.class);
            doReturn(sinkChain).when(sinkBroker).createMergingTransactionChain();

            //Creating the tree for config
            DOMDataTreeIdentifier confTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                    YangInstanceIdentifier.of());

            //Creating the tree for operational
            DOMDataTreeIdentifier operTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
                    YangInstanceIdentifier.of());

            NettyReplicationConfig config = new NettyReplicationConfig(support,
                    sinkBroker,
                    new InetSocketAddress(Inet4Address.getLoopbackAddress(), TEST_PORT),
                    Duration.ZERO,
                    Duration.ZERO,
                    3,
                    List.of(confTree, operTree));

            // Kick off the sink ...
            try (var sink = NettyReplicationSink.createSinkReg(css, config)) {
                // verify the connection was established for both trees
                verify(sinkBroker, timeout(1000).times(2)).createMergingTransactionChain();
                verify(sinkChain, timeout(2000).times(2)).newWriteOnlyTransaction();

                final var storeCaptor = ArgumentCaptor.forClass(LogicalDatastoreType.class);
                final var pathCaptor = ArgumentCaptor.forClass(YangInstanceIdentifier.class);
                final var dataCaptor = ArgumentCaptor.forClass(NormalizedNode.class);

                verify(sinkTx, timeout(2000).times(2)).put(
                        storeCaptor.capture(),
                        pathCaptor.capture(),
                        dataCaptor.capture());

                NormalizedNode capturedConfigState = null;
                NormalizedNode capturedOperationalState = null;

                for (int i = 0; i < storeCaptor.getAllValues().size(); ++i) {
                    final LogicalDatastoreType store = storeCaptor.getAllValues().get(i);
                    final YangInstanceIdentifier path = pathCaptor.getAllValues().get(i);
                    final NormalizedNode node = dataCaptor.getAllValues().get(i);

                    assertEquals(YangInstanceIdentifier.of(), path);

                    switch (store) {
                        case CONFIGURATION -> capturedConfigState = node;
                        case OPERATIONAL -> capturedOperationalState = node;
                        default -> fail("Unexpected datastore type " + store);
                    }
                }

                assertNotNull("CONFIGURATION initial state was not replicated", capturedConfigState);
                assertNotNull("OPERATIONAL initial state was not replicated", capturedOperationalState);

                assertEquals(expectedConfigState, capturedConfigState);
                assertEquals(expectedOperationalState, capturedOperationalState);

                verify(sinkTx, timeout(2000).times(2)).commit(any(), any());
            }
        }
    }

    private static ContainerNode generateConfiguationNormalizedNodeForEntities(final int amount) {
        final var builder = ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(new NodeIdentifier(ENTITY_QNAME));
        for (int i = 0; i < amount; i++) {
            final var name = "testEntity" + i;
            builder.withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(ENTITY_QNAME, ENTITY_NAME_QNAME, name))
                .build());
        }

        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
                .withChild(builder.build())
                .build();
    }

    private static ContainerNode generateOperationalNormalizedNodeForEntities(final int amount) {
        final var builder = ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(new NodeIdentifier(ENTITY_QNAME));
        for (int i = 0; i < amount; i++) {
            final var name = "testEntityOper" + i;
            builder.withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(ENTITY_QNAME, ENTITY_NAME_QNAME, name))
                    .build());
        }

        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
                .withChild(builder.build())
                .build();
    }

    private static void generateModification(final DataBroker broker, final int amount)
            throws InterruptedException, ExecutionException {
        for (int i = 0; i < amount; i++) {
            final WriteTransaction writeTransaction = broker.newWriteOnlyTransaction();
            final EntityKey key = new EntityKey("testEntity" + i);

            writeTransaction.put(LogicalDatastoreType.CONFIGURATION,
                DataObjectIdentifier.builder(Entity.class, key).build(), new EntityBuilder().withKey(key).build());
            writeTransaction.commit().get();

            //Quick "fix" of the test, insert the same mod into both stores.
            final WriteTransaction writeTransactionOper = broker.newWriteOnlyTransaction();
            final EntityKey keyOper = new EntityKey("testEntityOper" + i);
            writeTransactionOper.put(LogicalDatastoreType.OPERATIONAL,
                    DataObjectIdentifier.builder(Entity.class, keyOper).build(),
                    new EntityBuilder().withKey(keyOper).build());
            writeTransactionOper.commit().get();
        }
    }
}
