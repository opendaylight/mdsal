/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;

public record NettyReplicationConfig(BootstrapSupport bootstrapSupport,
                                     DOMDataBroker dataBroker,
                                     InetSocketAddress sourceAddress,
                                     Duration reconnectDelay,
                                     Duration keepaliveInterval,
                                     int maxMissedKeepalives,
                                     List<DOMDataTreeIdentifier> trees) {

    //validation - kept pattern from earlier code. Maybe improve validation error messages here?
    public NettyReplicationConfig {
        requireNonNull(bootstrapSupport);
        requireNonNull(dataBroker);
        requireNonNull(sourceAddress);
        requireNonNull(reconnectDelay);
        requireNonNull(keepaliveInterval);
        checkArgument(trees.size() > 0, "At least one tree must be configured");
        trees = normalizeTrees(trees);
        checkArgument(maxMissedKeepalives > 0,
                "max-missed-keepalives %s must be greater than 0",
                maxMissedKeepalives);
    }

    private static List<DOMDataTreeIdentifier> normalizeTrees(
            final List<DOMDataTreeIdentifier> trees) {
        requireNonNull(trees);

        final Set<DOMDataTreeIdentifier> uniqueTrees =
                new LinkedHashSet<>(trees);
        if (uniqueTrees.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one tree must be configured");
        }

        return List.copyOf(uniqueTrees);
    }
}
