/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.producer.netty;

import com.google.common.base.Preconditions;
import java.net.InetSocketAddress;

public class NettyBackupProducerConfiguration {

    private final String consumerAddress;
    private final int consumerPort;
    private final long connectionRetryIntervalMs;

    public NettyBackupProducerConfiguration(final String consumerAddress, final Integer consumerPort,
        final long connectionRetryIntervalMs) {
        this.consumerAddress = Preconditions.checkNotNull(consumerAddress,
            "Consumer address must be specified in the configuration");
        this.consumerPort = Preconditions.checkNotNull(consumerPort,
            "Consumer port must be specified in the configuration");
        this.connectionRetryIntervalMs = connectionRetryIntervalMs;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public int getConsumerPort() {
        return consumerPort;
    }

    public long getConnectionRetryIntervalMs() {
        return connectionRetryIntervalMs;
    }

    public InetSocketAddress getClientSocketAddress() {
        return new InetSocketAddress(consumerAddress, consumerPort);
    }
}
