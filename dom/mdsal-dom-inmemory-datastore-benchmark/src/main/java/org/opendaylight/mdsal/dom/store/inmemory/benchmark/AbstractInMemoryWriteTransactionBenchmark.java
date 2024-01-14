/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.benchmark;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public abstract class AbstractInMemoryWriteTransactionBenchmark {
    protected static final int OUTER_LIST_100K = 100000;
    protected static final int OUTER_LIST_50K = 50000;
    protected static final int OUTER_LIST_10K = 10000;

    protected static final YangInstanceIdentifier[] OUTER_LIST_100K_PATHS = initOuterListPaths(OUTER_LIST_100K);
    protected static final YangInstanceIdentifier[] OUTER_LIST_50K_PATHS = initOuterListPaths(OUTER_LIST_50K);
    protected static final YangInstanceIdentifier[] OUTER_LIST_10K_PATHS = initOuterListPaths(OUTER_LIST_10K);

    private static YangInstanceIdentifier[] initOuterListPaths(final int outerListPathsCount) {
        final YangInstanceIdentifier[] paths = new YangInstanceIdentifier[outerListPathsCount];

        for (int outerListKey = 0; outerListKey < outerListPathsCount; ++outerListKey) {
            paths[outerListKey] = YangInstanceIdentifier.builder(BenchmarkModel.OUTER_LIST_PATH)
                    .nodeWithKey(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, outerListKey).build();
        }
        return paths;
    }

    protected static final int WARMUP_ITERATIONS = 6;
    protected static final int MEASUREMENT_ITERATIONS = 6;

    protected static final SystemMapNode ONE_ITEM_INNER_LIST = initInnerListItems(1);
    protected static final SystemMapNode TWO_ITEM_INNER_LIST = initInnerListItems(2);
    protected static final SystemMapNode TEN_ITEM_INNER_LIST = initInnerListItems(10);

    private static SystemMapNode initInnerListItems(final int count) {
        final var mapBuilder = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(BenchmarkModel.INNER_LIST_QNAME));

        for (int i = 1; i <= count; ++i) {
            Integer key = i;
            mapBuilder.withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(BenchmarkModel.INNER_LIST_QNAME,
                    BenchmarkModel.NAME_QNAME, key))
                .withChild(ImmutableNodes.leafNode(BenchmarkModel.NAME_QNAME, key))
                .build());
        }
        return mapBuilder.build();
    }

    protected static final MapEntryNode[] OUTER_LIST_ONE_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_100K,
            ONE_ITEM_INNER_LIST);
    protected static final MapEntryNode[] OUTER_LIST_TWO_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_50K,
            TWO_ITEM_INNER_LIST);
    protected static final MapEntryNode[] OUTER_LIST_TEN_ITEM_INNER_LIST = initOuterListItems(OUTER_LIST_10K,
            TEN_ITEM_INNER_LIST);

    private static MapEntryNode[] initOuterListItems(final int outerListItemsCount, final SystemMapNode innerList) {
        final var outerListItems = new MapEntryNode[outerListItemsCount];

        for (int i = 0; i < outerListItemsCount; ++i) {
            Integer key = i;
            outerListItems[i] = ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(
                    NodeIdentifierWithPredicates.of(BenchmarkModel.OUTER_LIST_QNAME, BenchmarkModel.ID_QNAME, key))
                .withChild(ImmutableNodes.leafNode(BenchmarkModel.ID_QNAME, key))
                .withChild(innerList)
                .build();
        }
        return outerListItems;
    }

    protected EffectiveModelContext schemaContext;

    public abstract void setUp() throws Exception;

    public abstract void tearDown();

    protected static ContainerNode provideOuterListNode() {
        return ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BenchmarkModel.TEST_QNAME))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BenchmarkModel.OUTER_LIST_QNAME))
                    .build())
                .build();
    }
}
