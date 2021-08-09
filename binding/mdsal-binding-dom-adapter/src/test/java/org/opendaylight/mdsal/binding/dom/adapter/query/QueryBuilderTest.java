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
import java.util.Arrays;
import java.util.List;
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
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.AlarmMessage;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.AlarmUnion;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemKey;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.Alarms;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.AlarmsBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.alarms.AlarmStatus;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.alarms.AlarmStatusBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
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
        CODEC = new DefaultBindingCodecTreeFactory().create(BindingRuntimeHelpers.createRuntimeContext());
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
                            .setDerivedTypeLeaf(new AlarmMessage("message1"))
                            .setCritical(Empty.getInstance())
                            .setAlarmStatus(new AlarmStatusBuilder()
                                .setStatusCode(Uint16.valueOf(1))
                                .setStatusMessage("testStatus1")
                                .build())
                            .setAlarmMessages(Arrays.asList(
                                new AlarmMessage("message1"),
                                new AlarmMessage("message2"),
                                new AlarmMessage("message3")))
                            .setPrimitiveMessages(Arrays.asList("message1", "message2", "message3"))
                            .setAlarmUnions(Arrays.asList(
                                new AlarmUnion("123"),
                                new AlarmUnion(Uint32.valueOf(123)),
                                new AlarmUnion(new byte[]{1,2,3}),
                                new AlarmUnion(new AlarmMessage("nestedAlarmMessage"))))
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                            )).build(),
                        new AlarmsBuilder()
                            .setId(Uint64.ONE)
                            .setDerivedTypeLeaf(new AlarmMessage("message2"))
                            .setAlarmStatus(new AlarmStatusBuilder()
                                .setStatusCode(Uint16.valueOf(2))
                                .setStatusMessage("testStatus2")
                                .build())
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                            )).build(),
                        new AlarmsBuilder()
                            .setId(Uint64.TWO)
                            .setDerivedTypeLeaf(new AlarmMessage("message3"))
                            .setAlarmStatus(new AlarmStatusBuilder()
                                .setStatusCode(Uint16.valueOf(2))
                                .setStatusMessage("testStatus2")
                                .build())
                            .setCritical(Empty.getInstance())
                            .setAffectedUsers(BindingMap.of(
                                // TODO: fill
                                )).build())).build(),
                    new SystemBuilder().setName("second").setAlarms(BindingMap.of(
                        new AlarmsBuilder()
                            .setId(Uint64.ZERO)
                            .setCritical(Empty.getInstance())
                            .setDerivedTypeLeaf(new AlarmMessage("message1"))
                            .setAlarmMessages(Arrays.asList(
                                new AlarmMessage("message3"),
                                new AlarmMessage("message4"),
                                new AlarmMessage("message5")))
                            .setPrimitiveMessages(Arrays.asList("message3", "message4", "message5"))
                            .setAlarmUnions(Arrays.asList(
                                new AlarmUnion("123"),
                                new AlarmUnion(Uint32.valueOf(321)),
                                new AlarmUnion(new byte[]{3,2,1}),
                                new AlarmUnion(new AlarmMessage("nestedAlarmMessage"))))
                            .setAffectedUsers(BindingMap.of(
                            // TODO: fill
                        )).build()))
                        .build()
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
    public void testFindAlarmsContainingAlarmMessage() {
        findAlarmMessages(new AlarmMessage("message1"), 1, 1);
        findAlarmMessages(new AlarmMessage("message3"), 2, 2);
    }

    @Test
    public void testFindAlarmsContainingPrimitiveMessage() {
        findPrimitiveAlarmMessages("message1", 1, 1);
        findPrimitiveAlarmMessages("message3", 2, 2);
    }

    @Test
    public void testFindAlarmUnionsContainingValue() {
        findAlarmUnions(new AlarmUnion(Uint32.valueOf(123)), 1, 1);
        findAlarmUnions(new AlarmUnion("123"), 2, 2);
        findAlarmUnions(new AlarmUnion(new AlarmMessage("nestedAlarmMessage")), 2, 2);
        findAlarmUnions(new AlarmUnion(new AlarmMessage("missingAlarmMessage")), 0, 0);
    }

    @Test
    public void testFindAlarmByStatusObject() {
        findAlarmByStatus(buildStatus(1, "testStatus1"), 1, 1);
        findAlarmByStatus(buildStatus(2, "testStatus2"), 2, 2);
        findAlarmByStatus(buildStatus(1, "testStatus2"), 0, 0);
        findAlarmByStatus(buildStatus(1, null), 0, 0);
        findAlarmByStatus(new AlarmStatusBuilder()
            .setStatusCode((Uint16)null)
            .setStatusMessage("testStatus2")
            .build(), 0, 0);
        findAlarmByStatus(new AlarmStatusBuilder().setStatusCode(Uint16.valueOf(2)).build(), 0, 0);
        findAlarmByStatus(new AlarmStatusBuilder().setStatusMessage("testStatus2").build(), 0, 0);
    }

    @Test
    public void testFindAlarmWithDerivedTypeLeaf() {
        findAlarmByDerivedType(new AlarmMessage("message1"), 2, 2);
        findAlarmByDerivedType(new AlarmMessage("message2"), 1, 1);
        findAlarmByDerivedType(new AlarmMessage("messageX"), 0, 0);
    }

    @Test
    public void testFindAlarmWithoutStatusObject() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .specificChild(Alarms::getAlarmStatus).isNull().build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(1, items.size());
        assertEquals(1, values.size());

        LOG.info("Query built in {}", sw);
    }

    @Test
    public void testFindAlarmWithNonNullStatusObject() {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .specificChild(Alarms::getAlarmStatus).nonNull().build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(3, items.size());
        assertEquals(3, values.size());

        LOG.info("Query built in {}", sw);
    }

    private AlarmStatus buildStatus(int code, String msg) {
        return new AlarmStatusBuilder()
            .setStatusCode(Uint16.valueOf(code))
            .setStatusMessage(msg)
            .build();
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

    private void findAlarmMessages(final AlarmMessage message, final int expectedItems, final int expectedValues) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leafList(Alarms::getAlarmMessages).contains().item(message).build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(expectedItems, items.size());
        assertEquals(expectedValues, values.size());

        LOG.info("Query built in {}", sw);
    }

    private void findPrimitiveAlarmMessages(final String message, final int expectedItems, final int expectedValues) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leafList(Alarms::getPrimitiveMessages).contains().item(message).build();

        List<? extends Item<@NonNull Alarms>> items = execute(query).getItems();
        List<? extends Alarms> values = execute(query).getValues();
        assertEquals(expectedItems, items.size());
        assertEquals(expectedValues, values.size());

        LOG.info("Query built in {}", sw);
    }

    private void findAlarmUnions(final AlarmUnion union, final int expectedItems, final int expectedValues) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leafList(Alarms::getAlarmUnions).contains().item(union).build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(expectedItems, items.size());
        assertEquals(expectedValues, values.size());

        LOG.info("Query built in {}", sw);
    }

    private void findAlarmByStatus(final AlarmStatus status, final int expectedItems, final int expectedValues) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .specificChild(Alarms::getAlarmStatus).valueEquals(status).build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(expectedItems, items.size());
        assertEquals(expectedValues, values.size());

        LOG.info("Query built in {}", sw);
    }

    private void findAlarmByDerivedType(final AlarmMessage message, final int expectedItems, final int expectedValues) {
        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<Alarms> queryMessage = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
            .extractChild(Alarms.class)
            .matching()
            .leaf(Alarms::getDerivedTypeLeaf).valueEquals(message).build();

        List<? extends Item<@NonNull Alarms>> items = execute(queryMessage).getItems();
        List<? extends Alarms> values = execute(queryMessage).getValues();
        assertEquals(expectedItems, items.size());
        assertEquals(expectedValues, values.size());

        LOG.info("Query built in {}", sw);
    }
}
