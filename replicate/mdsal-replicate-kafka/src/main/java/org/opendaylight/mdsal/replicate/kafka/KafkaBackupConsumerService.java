/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaBackupConsumerService extends AbstractBackupConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBackupConsumerService.class);

    private static final long CONSUMER_SHUTDOWN_TIMEOUT = 10L;
    private static final String SINGLETON_IDENTIFIER = "KafkaBackupConsumerService";

    private KafkaConsumer<String, byte[]> messageConsumer;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private KafkaBackupConsumerConfiguration configuration;

    public KafkaBackupConsumerService(final DOMDataBroker domDataBroker) {
        super(domDataBroker);
    }

    @Override
    protected void initConsumer() throws IOException {
        configuration = KafkaBackupConsumerConfigurationBuilder.fromJson(getClass().getClassLoader()
                .getResourceAsStream("kafkaConsumer.json"));

        Thread currentThread = Thread.currentThread();
        ClassLoader savedClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(null);
        this.messageConsumer = new KafkaConsumer<>(configuration.getKafkaConsumerProperties());
        currentThread.setContextClassLoader(savedClassLoader);

        messageConsumer.subscribe(Arrays.asList(configuration.getMessageTopic()));
    }

    @Override
    protected synchronized void startConsumption() {
        LOG.info("Init KafkaBackupConsumerService");
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            ConsumerRecords<String, byte[]> records = messageConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, byte[]> record : records) {
                final DataTreeCandidate dataTreeCandidate;
                try {
                    dataTreeCandidate = DTCSerializer.deserializeDTC(record.value());
                } catch (IOException e) {
                    LOG.error("Could not deserialize DataTreeCandidate from received message.", e);
                    continue;
                }
                applyBackup(dataTreeCandidate);
            }
        }, 0, 1200, TimeUnit.MILLISECONDS);
    }

    @Override
    protected synchronized Boolean closeConsumer() {
        boolean shutdownSuccess = true;
        try {
            scheduledFuture.cancel(false);
            scheduler.schedule(() -> {
                messageConsumer.unsubscribe();
                messageConsumer.close(Duration.ZERO);
            }, 1, TimeUnit.SECONDS).get(CONSUMER_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
            LOG.info("KafkaConsumer shutdown completed");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
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
