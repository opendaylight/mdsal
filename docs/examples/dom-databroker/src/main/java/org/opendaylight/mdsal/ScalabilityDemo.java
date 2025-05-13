/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScalabilityDemo {
    private static final Logger LOG = LoggerFactory.getLogger(ScalabilityDemo.class);
    private static final int ITERATIONS = 1_000;
    private static final QName COUNTER_QNAME =
        QName.create("urn:test", "2025-05-13", "counter");

    public static void main(String[] args) throws InterruptedException {
        final var moduleInfos = BindingRuntimeHelpers.loadModuleInfos();
        final var schema = BindingRuntimeHelpers.createEffectiveModel(moduleInfos);
        final var dataBroker = StandaloneBrokerFactory.create(schema);
        new ScalabilityDemo().run(dataBroker);
    }

    private void run(DOMDataBroker broker) throws InterruptedException {
        final var successfulTx = new AtomicInteger();
        final var failedTx = new AtomicInteger();
        final var latch = new CountDownLatch(ITERATIONS);

        final var path = YangInstanceIdentifier.of(COUNTER_QNAME);
        final var startTime = System.nanoTime();

        for (int i = 0; i < ITERATIONS; ++i) {
            var tx = broker.newWriteOnlyTransaction();
            tx.put(OPERATIONAL, path, ImmutableNodes.leafNode(COUNTER_QNAME, i));
            tx.commit().addCallback(new FutureCallback<CommitInfo>() {
                @Override
                public void onSuccess(final CommitInfo result) {
                    LOG.debug("Device committed");
                    successfulTx.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onFailure(final Throwable cause) {
                    LOG.warn("Failed to commit device ", cause);
                    failedTx.incrementAndGet();
                    latch.countDown();
                }
            }, MoreExecutors.directExecutor());
        }

        latch.await();
        final var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        LOG.info("Total tx: {}  Success tx: {}  Failure tx: {}  Elapsed: {} ms  TPS: {}", ITERATIONS,
            successfulTx.get(), failedTx.get(), elapsed, (ITERATIONS * 1_000.0) / elapsed);
    }
}
