/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class NettyBackupConsumerConfigurationBuilder {

    public static final boolean DEFAULT_ENABLE_SERVER_AUTO_RESTART = false;

    private int listeningPort;
    private boolean enableServerAutoRestart;

    public static NettyBackupConsumerConfiguration fromJson(final InputStream jsonConfigInput) throws IOException {
        NettyBackupConsumerConfigurationBuilder builder = new NettyBackupConsumerConfigurationBuilder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode configNode = mapper.readTree(jsonConfigInput);
        jsonConfigInput.close();

        builder.setListeningPort(configNode.at("/netty-consumer/listening-port").asInt())
                .setEnableServerAutoRestart(configNode.at("/netty-consumer/enable-server-auto-restart")
                    .asBoolean(DEFAULT_ENABLE_SERVER_AUTO_RESTART));
        return builder.build();
    }

    public NettyBackupConsumerConfiguration build() {
        return new NettyBackupConsumerConfiguration(listeningPort, enableServerAutoRestart);
    }

    public NettyBackupConsumerConfigurationBuilder setListeningPort(final int listeningPort) {
        this.listeningPort = listeningPort;
        return this;
    }

    public NettyBackupConsumerConfigurationBuilder setEnableServerAutoRestart(final boolean enableServerAutoRestart) {
        this.enableServerAutoRestart = enableServerAutoRestart;
        return this;
    }

    public boolean isEnableServerAutoRestart() {
        return enableServerAutoRestart;
    }

    public int getListeningPort() {
        return listeningPort;
    }
}
