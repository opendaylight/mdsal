/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.QueryReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.api.query.QueryExpression;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryResult;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemBuilder;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemKey;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPerformanceTest extends AbstractDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(QueryPerformanceTest.class);
    private static final int SYSTEM_COUNT = 1_000_000;
    private static Foo FOO;

    private QueryFactory factory;

    @BeforeClass
    public static void beforeClass() {
        final Stopwatch sw = Stopwatch.createStarted();
        final BindingMap.Builder<SystemKey, System> builder = BindingMap.builder(SYSTEM_COUNT);

        for (int i = 0; i < SYSTEM_COUNT; ++i) {
            builder.add(new SystemBuilder().setName("name" + i).setAlias("alias" + i).build());
        }

        FOO = new FooBuilder().setSystem(builder.build()).build();
        LOG.info("Test data with {} items built in {}", SYSTEM_COUNT, sw);
    }

    @AfterClass
    public static void afterClass() {
        FOO = null;
    }

    @Override
    protected void setupWithRuntimeContext(final BindingRuntimeContext runtimeContext) {
        super.setupWithRuntimeContext(runtimeContext);
        factory = new DefaultQueryFactory(ServiceLoader.load(BindingCodecTreeFactory.class).findFirst().orElseThrow()
            .create(runtimeContext));
    }

    @Override
    protected void setupWithDataBroker(final DataBroker dataBroker) {
        final Stopwatch sw = Stopwatch.createStarted();
        WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();
        wtx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Foo.class), FOO);
        assertCommit(wtx.commit());
        LOG.info("Test data with {} items populated in {}", SYSTEM_COUNT, sw);
    }

    @Test
    public void queryLessThanAlarm() throws InterruptedException, ExecutionException {
        final String needle = "alias" + (SYSTEM_COUNT - 1);

        final Stopwatch sw = Stopwatch.createStarted();
        final QueryExpression<System> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
            .extractChild(System.class)
                .matching()
                    .leaf(System::getAlias).valueEquals(needle)
                .build();
        LOG.info("Query built in {}", sw);

        sw.reset().start();
        final FluentFuture<QueryResult<@NonNull System>> future;
        try (ReadTransaction rtx = getDataBroker().newReadOnlyTransaction()) {
            assertThat(rtx, instanceOf(QueryReadTransaction.class));
            future = ((QueryReadTransaction) rtx).execute(LogicalDatastoreType.CONFIGURATION, query);
        }

        final QueryResult<@NonNull System> result = future.get();
        LOG.info("Query executed {} in {}", future, sw);

        assertTrue(result.stream().findAny().isPresent());
        LOG.info("Query result in {}", sw);
    }

    @Test
    public void searchLessThanAlarm() throws InterruptedException, ExecutionException {
        final String needle = "alias" + (SYSTEM_COUNT - 1);

        final Stopwatch sw = Stopwatch.createStarted();
        final FluentFuture<Optional<Foo>> future;
        try (ReadTransaction rtx = getDataBroker().newReadOnlyTransaction()) {
            future = rtx.read(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Foo.class));
        }

        final Foo haystack = future.get().orElseThrow();
        LOG.info("Read executed in {}", sw);

        Object result = null;
        for (System system : haystack.nonnullSystem().values()) {
            if (needle.equals(system.getAlias())) {
                result = system;
                break;
            }
        }

        LOG.info("Search found {} in {}", result, sw);
        assertNotNull(result);
    }
}
