/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.benchmark;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStore;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class InMemoryBrokerWriteTransactionBenchmark extends AbstractInMemoryBrokerWriteTransactionBenchmark {
    private List<InMemoryDOMStore> stores = List.of();
    private ExecutorService executor = null;

    @Setup(Level.Trial)
    @Override
    public void setUp() throws Exception {
        modelContext = BenchmarkModel.createTestContext();
        executor = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor) Executors.newFixedThreadPool(1), 1L,
                        TimeUnit.SECONDS);

        var configStore = newIMDS("CFG", CONFIGURATION, MoreExecutors.newDirectExecutorService());
        var operStore = newIMDS("OPER", OPERATIONAL, MoreExecutors.newDirectExecutorService());
        stores = List.of(configStore, operStore);
        domBroker = new SerializedDOMDataBroker(Map.of(CONFIGURATION, configStore, OPERATIONAL, operStore), executor);

        initTestNode();
    }

    @Override
    public void tearDown() {
        domBroker.close();
        executor.shutdownNow();
        stores.parallelStream().forEach(InMemoryDOMStore::close);
    }
}
