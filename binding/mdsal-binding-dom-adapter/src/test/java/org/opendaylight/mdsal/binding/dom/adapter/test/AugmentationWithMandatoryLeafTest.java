/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.interfaces.rev210426.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.Resource;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.ResourceBuilder;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.reporting.EthernetBuilder;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.resource.resource.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.TopService;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.TopServiceBuilder;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.top.service.Services;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.top.service.ServicesBuilder;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.top.service.ServicesKey;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.top.service.services.Topology;
import org.opendaylight.yang.gen.v1.dummy.org.openroadm.service.rev210426.top.service.services.TopologyBuilder;
import org.opendaylight.yang.gen.v1.dummy.topology.rev210426.hop.Interface1;
import org.opendaylight.yang.gen.v1.dummy.topology.rev210426.hop.Interface1Builder;
import org.opendaylight.yang.gen.v1.dummy.topology.rev210426.topology.AToZ;
import org.opendaylight.yang.gen.v1.dummy.topology.rev210426.topology.AToZBuilder;
import org.opendaylight.yang.gen.v1.dummy.topology.rev210426.topology.AToZKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;

public class AugmentationWithMandatoryLeafTest extends AbstractBaseDataBrokerTest {

    private static final String TEST_A_TO_Z_ID = "test-aToZ-id";
    private static final String TEST_SERVICE_NAME = "test-service-name";

    private static final QName TOP_SERVICE_QNAME = QName
        .create("dummy:org-openroadm-service", "2021-04-26", "top-service");
    private static final QName ETHERNET_CSMACD_QNAME = QName
        .create("dummy:org-openroadm-interfaces", "2021-04-26", "ethernetCsmacd");

    private static final QName TOPOLOGY_QNAME = QName.create(TOP_SERVICE_QNAME, "topology");
    private static final QName RESOURCE_QNAME = QName.create(TOP_SERVICE_QNAME, "resource");
    private static final QName ETHERNET_CONT_QNAME = QName.create(TOP_SERVICE_QNAME, "ethernet");
    private static final QName ETHERNET_SPEED_QNAME = QName.create(TOP_SERVICE_QNAME, "speed");
    private static final QName TYPE_QNAME = QName.create(TOP_SERVICE_QNAME, "type");
    private static final QName CHOICE_RESOURCE_QNAME = QName.create(TOP_SERVICE_QNAME, "resource");
    private static final NodeIdentifier CHOICE_RESOURCE_ID = new NodeIdentifier(CHOICE_RESOURCE_QNAME);

    private static final QName A_TO_Z_LIST_QNAME = QName.create(TOP_SERVICE_QNAME, "aToZ");
    private static final QName A_TO_Z_LIST_KEY_QNAME = QName.create(TOP_SERVICE_QNAME, "id");
    private static final Map<QName, Object> A_TO_Z_PREDICATES = ImmutableMap.of(A_TO_Z_LIST_KEY_QNAME, TEST_A_TO_Z_ID);

    private static final QName SERVICES_LIST_QNAME = QName.create(TOP_SERVICE_QNAME, "services");
    private static final QName SERVICES_LIST_KEY_LEAF_QNAME = QName.create(TOP_SERVICE_QNAME, "service-name");
    private static final Map<QName, Object> SERVICES_PREDICATES = ImmutableMap.of(SERVICES_LIST_KEY_LEAF_QNAME, TEST_SERVICE_NAME);

    private static final YangInstanceIdentifier TOP_SERVICE_PATH = YangInstanceIdentifier.of(TOP_SERVICE_QNAME);

    private ConcurrentDataBrokerTestCustomizer testCustomizer;

    @Override
    protected AbstractDataBrokerTestCustomizer createDataBrokerTestCustomizer() {
        if (this.testCustomizer == null) {
            this.testCustomizer = new ConcurrentDataBrokerTestCustomizer(false);
        }
        return this.testCustomizer;
    }

    /**
     * Write data containing mandatory leaf "type" in augmentation via binding DataBroker.
     * This fails with exception:
     *    IllegalArgumentException: Node (dummy:org-openroadm-service?revision=2021-04-26)resource is missing mandatory
     *    descendant /(dummy:org-openroadm-service?revision=2021-04-26)type
     */
    @Test
    public void testWriteViaBindingBroker() throws ExecutionException, InterruptedException {
        DataBroker broker = this.getDataBroker();
        InstanceIdentifier<TopService> serviceListIid =
            InstanceIdentifier.create(TopService.class);

        TopService serviceListData = new TopServiceBuilder()
            .setServices(Arrays.asList(new ServicesBuilder()
                .setServiceName(TEST_SERVICE_NAME)
                .setTopology(new TopologyBuilder()
                    .setAToZ(Arrays.asList(new AToZBuilder()
                        .setId(TEST_A_TO_Z_ID)
                        .setResource(new ResourceBuilder()
                            .setResource(new InterfaceBuilder()
                                .addAugmentation(Interface1.class, new Interface1Builder()
                                    .setType(EthernetCsmacd.class)
                                    .setEthernet(new EthernetBuilder()
                                        .setSpeed(123L)
                                        .build())
                                    .build())
                                .build())
                            .build())
                        .build()))
                    .build())
                .build()))
            .build();

        WriteTransaction wt = broker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.CONFIGURATION, serviceListIid, serviceListData);

        wt.commit().get();
        Resource resource = readResourceContainerCaseViaDataBroker();
        org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.resource.Resource
            caseResource = resource.getResource();

    }

    /**
     * Using DOMDataBroker write the mandatory leaf as a direct child of the parent choice container.
     * Notice 2 reads here:
     * 1 - reading the leaf via DOMDataBroker passes and shows the leaf is indeed present.
     * 2 - reading via binding DataBroker fails with NPE - more info bellow.
     */
    @Test
    public void testWriteDataWithMandatoryLeafAsDirectChild() throws ExecutionException, InterruptedException {
        ContainerNode testResource = ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(NodeIdentifier.create(RESOURCE_QNAME))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(CHOICE_RESOURCE_ID)
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(TYPE_QNAME))
                    .withValue(ETHERNET_CSMACD_QNAME)
                    .build())
                .withChild(Builders
                    .containerBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(ETHERNET_CONT_QNAME))
                    .withChild(Builders
                        .leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(ETHERNET_SPEED_QNAME))
                        .withValue(123)
                        .build())
                    .build())
                    .build())
                .build();

        ContainerNode topServiceData = generateTopServiceDataWithResource(testResource);

        DOMDataTreeWriteTransaction wt = getDomBroker().newWriteOnlyTransaction();

        wt.put(LogicalDatastoreType.CONFIGURATION, TOP_SERVICE_PATH, topServiceData);
        wt.commit().get();

        // read the resource via DOMDataBroker and verify the augmented mandatory leaf "type" was written correctly
        NormalizedNode<?, ?> resourceCaseNode = readResourceCaseViaDOMDataBroker();
        Optional<NormalizedNode<?, ?>> mandatoryTypeNode = NormalizedNodes.findNode(resourceCaseNode,
            YangInstanceIdentifier.of(TYPE_QNAME));
        Assert.assertTrue(mandatoryTypeNode.isPresent());
        Assert.assertEquals(mandatoryTypeNode.get().getValue(), ETHERNET_CSMACD_QNAME);

        // Now read the resource via binding DataBroker and try to retrieve the instance of the resource case.
        // This fails because the ChoiceNodeCodecContext.deserialize() can't find the leaf "type".
        // It is looking for identifier:
        //
        //    ImmutableLeafNode{identifier=(dummy:org-openroadm-service?revision=2021-04-26)type,
        //                      value=(dummy:org-openroadm-interfaces?revision=2021-04-26)ethernetCsmacd}
        //
        // among the cases, but this identifier is wrapped in:
        //
        //    AugmentationIdentifier{childNames=[(dummy:org-openroadm-service?revision=2021-04-26)type,
        //                                       (dummy:org-openroadm-service?revision=2021-04-26)ethernet]}


        Resource resource = readResourceContainerCaseViaDataBroker();
        org.opendaylight.yang.gen.v1.dummy.org.openroadm.resource.rev210426.resource.resource.Resource
            caseResource = resource.getResource();
    }

    /**
     * Using DOMDataBroker wrap the mandatory leaf inside of AugmentationNode and write that as a child of the choice
     * container.
     * This results in the same exception as writing via binding DataBroker - {@link #testWriteViaBindingBroker()}
     */
    @Test
    public void testWriteDataWithMandatoryLeafInsideAugmentation() throws ExecutionException, InterruptedException {
        ContainerNode testResource = ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(NodeIdentifier.create(RESOURCE_QNAME))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(CHOICE_RESOURCE_ID)
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(AugmentationIdentifier.create(Sets.newSet(TYPE_QNAME, ETHERNET_CONT_QNAME)))
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(TYPE_QNAME))
                        .withValue(ETHERNET_CSMACD_QNAME)
                        .build())
                    .withChild(Builders
                        .containerBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(ETHERNET_CONT_QNAME))
                        .withChild(Builders
                            .leafBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(ETHERNET_SPEED_QNAME))
                            .withValue(123)
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        ContainerNode topServiceData = generateTopServiceDataWithResource(testResource);

        DOMDataTreeWriteTransaction wt = getDomBroker().newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.CONFIGURATION, TOP_SERVICE_PATH, topServiceData);

        wt.commit().get();
    }

    private Resource readResourceContainerCaseViaDataBroker() throws ExecutionException, InterruptedException {
        Optional<Resource> readResource = getDataBroker().newReadOnlyTransaction()
            .read(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(TopService.class)
                    .child(Services.class, new ServicesKey(TEST_SERVICE_NAME))
                    .child(Topology.class)
                    .child(AToZ.class, new AToZKey(TEST_A_TO_Z_ID))
                    .child(Resource.class))
            .get();
        if (readResource.isPresent()) {
            return readResource.get();
        }
        throw new IllegalStateException("Failed to read test resource");
    }

    private NormalizedNode<?, ?> readResourceCaseViaDOMDataBroker() throws ExecutionException, InterruptedException {
        Optional<NormalizedNode<?, ?>> readResource =
            getDomBroker().newReadOnlyTransaction().read(LogicalDatastoreType.CONFIGURATION,
                YangInstanceIdentifier.builder()
                    .node(TopService.QNAME)
                    .node(Services.QNAME)
                    .nodeWithKey(Services.QNAME, SERVICES_PREDICATES)
                    .node(Topology.QNAME).node(A_TO_Z_LIST_QNAME)
                    .nodeWithKey(A_TO_Z_LIST_QNAME, A_TO_Z_PREDICATES)
                    .node(RESOURCE_QNAME)
                    .node(RESOURCE_QNAME)
                    .build())
                .get();
        if (readResource.isPresent()) {
            return readResource.get();
        }
        throw new IllegalStateException("Failed to read test resource case");
    }

    private ContainerNode generateTopServiceDataWithResource(ContainerNode resourceNode) {
        MapNode mapNodeAtoZListWithNodes = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(A_TO_Z_LIST_QNAME))
            .withChild(ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(A_TO_Z_LIST_QNAME, A_TO_Z_LIST_KEY_QNAME,
                    TEST_A_TO_Z_ID))
                .withChild(resourceNode)
                .build())
            .build();

        MapNode services = Builders.mapBuilder().withNodeIdentifier(NodeIdentifier.create(SERVICES_LIST_QNAME))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(SERVICES_LIST_QNAME, SERVICES_LIST_KEY_LEAF_QNAME,
                    TEST_SERVICE_NAME))
                .withChild(Builders.containerBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(TOPOLOGY_QNAME))
                    .withChild(mapNodeAtoZListWithNodes).build()).build()).build();

        ContainerNode topServiceContainer = Builders.containerBuilder()
            .withNodeIdentifier(NodeIdentifier.create(TOP_SERVICE_QNAME))
            .withChild(services)
            .build();

        return topServiceContainer;
    }
}
