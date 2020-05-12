/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.consumer;

import com.google.common.base.Preconditions;

public class NettyBackupConsumerConfiguration {

    private final int listeningPort;
    private final boolean enableServerAutoRestart;

    public NettyBackupConsumerConfiguration(final Integer listeningPort, final boolean enableServerAutoRestart) {
        this.listeningPort = Preconditions.checkNotNull(listeningPort,
            "Listening port must be specified in the configuration");
        this.enableServerAutoRestart = enableServerAutoRestart;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public boolean isEnableServerAutoRestart() {
        return enableServerAutoRestart;
    }
}
