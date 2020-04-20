/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaBackupConsumerService extends AbstractBackupConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBackupConsumerService.class);

    private static final long CONSUMER_SHUTDOWN_TIMEOUT = 5L;
    private static final String SINGLETON_IDENTIFIER = "KafkaBackupConsumerService";

    private KafkaConsumer<String, byte[]> messageConsumer;
    private ScheduledFuture<?> scheduledFuture;
    private ScheduledExecutorService scheduler;
    private KafkaBackupConsumerConfiguration configuration;

    public KafkaBackupConsumerService(final DOMDataBroker domDataBroker) {
        super(domDataBroker);
    }

    @Override
    protected void initConsumer() throws IOException {
        configuration = KafkaBackupConsumerConfigurationBuilder.fromJson(getClass().getClassLoader()
                .getResourceAsStream("kafkaConsumer.json"));
        scheduler = Executors.newScheduledThreadPool(0);

        Thread currentThread = Thread.currentThread();
        ClassLoader savedClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(null);
        this.messageConsumer = new KafkaConsumer<>(configuration.getKafkaConsumerProperties());
        currentThread.setContextClassLoader(savedClassLoader);

        messageConsumer.subscribe(Arrays.asList(configuration.getMessageTopic()));
    }

    @Override
    protected void startConsumption() {
        LOG.info("Init KafkaBackupConsumerService");
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            ConsumerRecords<String, byte[]> records = messageConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, byte[]> record : records) {
                try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(record.value()))) {
                    NormalizedNodeDataInput nodeDataInput = NormalizedNodeDataInput.newDataInput(dataInputStream);
                    DataTreeCandidate dataTreeCandidate =
                            DataTreeCandidateInputOutput.readDataTreeCandidate(nodeDataInput,
                            ReusableImmutableNormalizedNodeStreamWriter.create());
                    LOG.info("Received record - DataTreeCandidate: {}", dataTreeCandidate.getRootNode().toString());
                    applyBackup(dataTreeCandidate);
                } catch (IOException e) {
                    LOG.error("Couldn't deserialize DataTreeCandidate from received message.");
                }
            }
        }, 0, 1200, TimeUnit.MILLISECONDS);
    }

    @Override
    protected synchronized Boolean closeConsumer() {
        boolean shutdownSuccess = true;
        try {
            scheduler.schedule(() -> {
                messageConsumer.unsubscribe();
                messageConsumer.close(Duration.ZERO);
            }, 1, TimeUnit.SECONDS).get();
            LOG.info("KafkaConsumer shutdown completed");
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("KafkaConsumer didn't close properly!", e);
            shutdownSuccess = false;
        }
        scheduler.shutdown();
        return shutdownSuccess;
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(SINGLETON_IDENTIFIER);
    }
}
