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
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.query.QueryExecutor;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.api.query.QueryResult.Item;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemKey;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.Alarms;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.AlarmsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(QueryBuilderTest.class);
    private static BindingCodecTree CODEC;

    private final QueryFactory factory = new DefaultQueryFactory(CODEC);
    private QueryExecutor executor;

    @BeforeClass
    public static final void beforeClass() {
        CODEC = ServiceLoader.load(BindingCodecTreeFactory.class).findFirst().orElseThrow()
            .create(BindingRuntimeHelpers.createRuntimeContext());
    }

    @AfterClass
    public static final void afterClass() {
        CODEC = null;
    }

    @Before
    public void before() {
        executor = SimpleQueryExecutor.builder(CODEC)
            .add(new FooBuilder()
                .setSystem(BindingMap.of(
                    new SystemBuilder().setName("first").setAlarms(BindingMap.of(
                        new AlarmsBuilder()
                            .setId(Uint64.ZERO)
                            .setCritical(Empty.value())
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
                            .setCritical(Empty.value())
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                                )).build())).build(),
                    new SystemBuilder().setName("second").setAlarms(BindingMap.of(
                        new AlarmsBuilder()
                        .setId(Uint64.ZERO)
                        .setCritical(Empty.value())
                        .setAffectedUsers(BindingMap.of(
                            // TODO: fill
                        )).build())).build()
                    ))
                .build())
            .build();
    }

    @Test
    public void bar() {
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
    public void testFindCriticalAlarms() {
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
    public void testFindNonCriticalAlarms() {
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
    public void testFindZeroAlarms() {
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

        List<Alarms> verifiedResult = items.stream()
            .map(Item::object)
            .filter(object -> object.getId().equals(Uint64.ZERO))
            .collect(Collectors.toList());
        assertNotNull(verifiedResult);
        assertEquals(2, verifiedResult.size());
    }

    @Test
    public void testFindSystemFirstAlarmOne() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class, new SystemKey("first"))
            .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getId).valueEquals(Uint64.ZERO)
                .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(1, items.size());
    }

    @Test
    public void testFindGreaterThanAlarms() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leaf(Alarms::getId).greaterThan(Uint64.ONE)
            .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(1, items.size());
    }

    @Test
    public void testFindGreaterThanOrEqualsAlarms() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leaf(Alarms::getId).greaterThanOrEqual(Uint64.ONE)
            .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(2, items.size());
    }

    @Test
    public void testFindLessThanAlarms() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leaf(Alarms::getId).lessThan(Uint64.ONE)
            .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(2, items.size());
    }

    @Test
    public void testFindLessThanOrEqualsAlarms() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leaf(Alarms::getId).lessThanOrEqual(Uint64.ONE)
            .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        assertEquals(3, items.size());
    }

    @Test
    public void testFindSystemFirstWithAlarmOne() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<System> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class, new SystemKey("first"))
                .matching()
                    .childObject(Alarms.class)
                        .leaf(Alarms::getId).valueEquals(Uint64.ZERO)
                .build();
        LOG.info("Query built in {}", sw);

        final List<? extends Item<@NonNull System>> items = execute(query).getItems();
        assertEquals(1, items.size());
    }


    private <T extends @NonNull DataObject> QueryResult<T> execute(final QueryExpression<T> query) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryResult<T> result = executor.executeQuery(query);
        LOG.info("Query executed in {}", sw);
        return result;
    }
}
