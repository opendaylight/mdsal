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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClusteredDOMDataTreeChangeListener which listens on the root of CONFIG dataTree. This means that any modification
 * to the dataTree is caught and sent to the Kafka stream. There should be a BackupConsumer running on the backup
 * site which is connected to the same Kafka stream. The consumer will then apply the received modifications to the
 * backup site.
 * */
public class KafkaBackupProducerService extends AbstractBackupProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaBackupProducerService.class);

    private static final String SINGLETON_IDENTIFIER = "KafkaBackupProducerService";

    private KafkaBackupProducerConfiguration configuration;
    private KafkaProducer<String, byte[]> messageProducer;

    public KafkaBackupProducerService(final DOMDataBroker domDataBroker) {
        super(domDataBroker);
    }

    @Override
    protected void initProducer() throws IOException {
        LOG.info("Init KafkaBackupProducerService");
        configuration = KafkaBackupProducerConfigurationBuilder.fromJson(getClass().getClassLoader()
                .getResourceAsStream("kafkaProducer.json"));

        Thread currentThread = Thread.currentThread();
        ClassLoader savedClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(null);
        messageProducer = new KafkaProducer<>(configuration.getKafkaProducerProperties());
        currentThread.setContextClassLoader(savedClassLoader);
    }

    @Override
    protected void sendToBackup(final DataTreeCandidate candidate) {
        LOG.info("Producer - sending message - {}", candidate.getRootNode().getDataAfter().toString());
        final byte[] serializedCandidate;
        try {
            serializedCandidate = DTCSerializer.serializeDTC(candidate);
        } catch (IOException e) {
            LOG.error("Failed to serialize candidate", e);
            return;
        }

        try {
            messageProducer.send(new ProducerRecord<>(configuration.getMessageTopic(),
                    configuration.getMessagePartition(), configuration.getMessageKey(), serializedCandidate))
                .get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Producer - failed to send message", e);
        }
    }

    @Override
    protected synchronized Boolean closeBackupProducer() {
        messageProducer.close(Duration.ZERO);
        LOG.info("Backup Producer API instance closed");
        return true;
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return ServiceGroupIdentifier.create(SINGLETON_IDENTIFIER);
    }
}
