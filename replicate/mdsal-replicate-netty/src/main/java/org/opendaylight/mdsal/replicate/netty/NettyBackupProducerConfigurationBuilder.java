/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class NettyBackupProducerConfigurationBuilder {

    public static final long DEFAULT_CONNECTION_RETRY_INTERVAL_MS = 1000;

    private String consumerAddress;
    private int consumerPort;
    private long connectionRetryIntervalMs;

    public static NettyBackupProducerConfiguration fromJson(final InputStream jsonConfigInput) throws IOException {
        NettyBackupProducerConfigurationBuilder builder = new NettyBackupProducerConfigurationBuilder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode configNode = mapper.readTree(jsonConfigInput);
        jsonConfigInput.close();

        builder.setConsumerAddress(configNode.at("/netty-connection/consumer-address").asText())
                .setConsumerPort(configNode.at("/netty-connection/consumer-port").asInt())
                .setConnectionRetryIntervalMs(configNode.at("/netty-connection/connection-retry-interval-ms")
                    .asLong(DEFAULT_CONNECTION_RETRY_INTERVAL_MS));
        return builder.build();
    }

    public NettyBackupProducerConfiguration build() {
        return new NettyBackupProducerConfiguration(consumerAddress, consumerPort, connectionRetryIntervalMs);
    }

    public NettyBackupProducerConfigurationBuilder setConsumerAddress(final String consumerAddress) {
        this.consumerAddress = consumerAddress;
        return this;
    }

    public NettyBackupProducerConfigurationBuilder setConsumerPort(final int consumerPort) {
        this.consumerPort = consumerPort;
        return this;
    }

    public NettyBackupProducerConfigurationBuilder setConnectionRetryIntervalMs(final long connectionRetryIntervalMs) {
        this.connectionRetryIntervalMs = connectionRetryIntervalMs;
        return this;
    }

    public int getConsumerPort() {
        return consumerPort;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public long getConnectionRetryIntervalMs() {
        return connectionRetryIntervalMs;
    }
}
