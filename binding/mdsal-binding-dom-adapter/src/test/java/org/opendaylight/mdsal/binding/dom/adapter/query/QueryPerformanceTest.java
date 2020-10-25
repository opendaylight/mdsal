/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Stopwatch;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPerformanceTest extends AbstractQueryTest {
    private static final Logger LOG = LoggerFactory.getLogger(QueryPerformanceTest.class);
    private static final int SYSTEM_COUNT = 1000000;

    private Foo bindingData;

    @Before
    public void before() {
        final BindingMap.Builder<SystemKey, System> builder = BindingMap.orderedBuilder(SYSTEM_COUNT);

        for (int i = 0; i < SYSTEM_COUNT; ++i) {
            builder.add(new SystemBuilder().setName("name" + i).setAlias("alias" + i).build());
        }

        bindingData = new FooBuilder().setSystem(builder.build()).build();
    }

    @Test
    public void findLessThanAlarm() {
        final String needle = "alias" + (SYSTEM_COUNT - 1);
        final QueryExecutor haystack = SimpleQueryExecutor.builder(CODEC).add(bindingData).build();

        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<System> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
                .matching()
                    .leaf(System::getAlias).valueEquals(needle)
                .build();
        LOG.info("Query built in {}", sw);

        final QueryResult<@NonNull System> result = haystack.executeQuery(query);

        LOG.info("Query result {} in {}", result, sw);
        assertEquals(1, result.stream().count());
    }

    @Test
    public void searchLessThanAlarm() {
        final String needle = "alias" + (SYSTEM_COUNT - 1);

        // Make things equal: both are operating on DOM-backed data
        final BindingDataObjectCodecTreeNode<Foo> codec = CODEC.getSubtreeCodec(InstanceIdentifier.create(Foo.class));
        final NormalizedNode<?, ?> domData = codec.serialize(bindingData);
        final Foo hayStack = codec.deserialize(domData);

        final Stopwatch sw = Stopwatch.createStarted();
        Object result = null;
        for (System system : hayStack.nonnullSystem().values()) {
            if (needle.equals(system.getAlias())) {
                result = system;
                break;
            }
        }

        LOG.info("Search found {} in {}", result, sw);
        assertNotNull(result);
    }
}
