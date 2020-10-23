/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Stopwatch;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.api.query.QueryResult.Item;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.Alarms;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.AlarmsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(QueryBuilderTest.class);
    private static BindingCodecTree CODEC;
    private static QueryExecutor EXECUTOR;

    private final QueryFactory factory = new DefaultQueryFactory(CODEC);

    @BeforeClass
    public static void beforeClass() {
        CODEC = new DefaultBindingCodecTreeFactory().create(BindingRuntimeHelpers.createRuntimeContext());
        EXECUTOR = SimpleQueryExecutor.builder(CODEC)
            .add(new FooBuilder()
                .setSystem(BindingMap.of(
                    new SystemBuilder().setName("first").setAlarms(BindingMap.of(
                        new AlarmsBuilder()
                            .setId(Uint64.ZERO)
                            .setCritical(Empty.getInstance())
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                            )).build(),
                        new AlarmsBuilder()
                            .setId(Uint64.ONE)
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                            )).build(),
                        new AlarmsBuilder()
                            .setId(Uint64.TWO)
                            .setCritical(Empty.getInstance())
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                                )).build())).build(),
                    new SystemBuilder().setName("second").setAlarms(BindingMap.of(
                        new AlarmsBuilder()
                        .setId(Uint64.ZERO)
                        .setCritical(Empty.getInstance())
                        .setAffectedUsers(BindingMap.of(
                            // TODO: fill
                        )).build())).build()
                    ))
                .build())
            .build();
    }

    @AfterClass
    public static void afterClass() {
        CODEC = null;
        EXECUTOR = null;
    }

    @Test
    public void bar() throws QueryStructureException {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<TopLevelList> query = factory.querySubtree(InstanceIdentifier.create(Top.class))
                .extractChild(TopLevelList.class)
                .matching()
                    .childObject(NestedList.class)
                    .leaf(NestedList::getName).contains("foo")
                    .and().leaf(TopLevelList::getName).valueEquals("bar")
                .build();
        LOG.info("Query built in {}", sw);

        assertEquals(0, execute(query).getItems().size());
    }

    @Test
    public void testFindCriticalAlarms() throws QueryStructureException {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getCritical).nonNull()
                .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(3, items.size());
    }

    @Test
    public void testFindNonCriticalAlarms() throws QueryStructureException {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getCritical).isNull()
                .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(1, items.size());
    }

    @Test
    public void testFindZeroAlarms() throws QueryStructureException {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getId).valueEquals(Uint64.ZERO)
                .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(2, items.size());
    }

    private static <T extends @NonNull DataObject> QueryResult<T> execute(final QueryExpression<T> query) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryResult<T> result = EXECUTOR.executeQuery(query);
        LOG.info("Query executed in {}", sw);
        return result;
    }
}
