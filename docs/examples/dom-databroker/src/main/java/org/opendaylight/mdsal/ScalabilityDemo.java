/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.OnCommitCallback;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.mdsal.dom.store.inmemory.dagger.InMemoryDOMStoreFactoryModule;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScalabilityDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ScalabilityDemo.class);
    private static final int ITERATIONS = 1_000;
    private static final QName COUNTER_QNAME = QName.create("urn:test", "2025-05-13", "counter");

    private ScalabilityDemo() {
        // Hidden on purpose
    }

    public static void main(final String[] args) throws InterruptedException {
        // very simple setup:
        // - discover all models on the current class loader and wrap them in a DOMSchemaService
        final var schemaService = new FixedDOMSchemaService(
            BindingRuntimeHelpers.createEffectiveModel(BindingRuntimeHelpers.loadModuleInfos()));
        // - instantiate an InMemoryDOMStore with default parameters for operational
        final var domStoreFactory = InMemoryDOMStoreFactoryModule.provideInMemoryDOMStoreFactory(
            ReferenceDataTreeFactoryModule.provideDataTreeFactory());
        try (var operStore = domStoreFactory.create("OPER", DataTreeConfiguration.DEFAULT_OPERATIONAL, schemaService)) {
            // - a serialized data broker with a single-threaded commit queue, dispatching listeners on the same thread
            // Note that SerializedDOMDataBroker does not take ownership of the service.
            // Production environments use a more complicated setup, where the commit queue is single threaded, but
            // listener dispatch occurs concurrently.
            // FIXME: replicate that setup here
            try (var executor = Executors.newSingleThreadExecutor()) {
                try (var dataBroker = new SerializedDOMDataBroker(Map.of(OPERATIONAL, operStore), executor)) {
                    run(dataBroker);
                }
            }
        }
    }

    private static void run(final DOMDataBroker broker) throws InterruptedException {
        final var successfulTx = new AtomicInteger();
        final var failedTx = new AtomicInteger();
        final var observedEvents = new AtomicInteger();
        final var latch = new CountDownLatch(ITERATIONS);

        final var path = YangInstanceIdentifier.of(COUNTER_QNAME);
        final var startTime = System.nanoTime();

        // Create the listener
        final var listener = new DOMDataTreeChangeListener() {
            @Override
            public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
                observedEvents.addAndGet(changes.size());
            }

            @Override
            public void onInitialData() {
                // Nothing to do when there is no pre-existing data
            }
        };
        final var changeExt = broker.extension(DOMDataBroker.DataTreeChangeExtension.class);
        changeExt.registerTreeChangeListener(DOMDataTreeIdentifier.of(OPERATIONAL, path), listener);

        for (int i = 0; i < ITERATIONS; ++i) {
            final var tx = broker.newWriteOnlyTransaction();
            tx.put(OPERATIONAL, path, ImmutableNodes.leafNode(COUNTER_QNAME, i));
            tx.commit(new OnCommitCallback() {
                @Override
                public void onSuccess(final CommitInfo commitInfo) {
                    LOG.debug("Counter committed");
                    successfulTx.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onFailure(final TransactionCommitFailedException cause) {
                    LOG.warn("Failed to commit counter", cause);
                    failedTx.incrementAndGet();
                    latch.countDown();
                }
            }, MoreExecutors.directExecutor());
        }

        latch.await();
        final var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        LOG.info("Total tx: {}  Success tx: {}  Failure tx: {} Observed by listener tx: {} Elapsed: {} ms  TPS: {}",
            ITERATIONS, successfulTx.get(), failedTx.get(), observedEvents, elapsed, ITERATIONS * 1_000.0 / elapsed);
    }
}
