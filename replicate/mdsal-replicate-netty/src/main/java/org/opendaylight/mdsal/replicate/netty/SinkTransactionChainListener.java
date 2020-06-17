/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static java.util.Objects.requireNonNull;

import io.netty.channel.Channel;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkTransactionChainListener implements DOMTransactionChainListener {
    private static final Logger LOG = LoggerFactory.getLogger(SinkTransactionChainListener.class);

    private final Channel channel;

    SinkTransactionChainListener(final Channel channel) {
        this.channel = requireNonNull(channel);
    }

    @Override
    public void onTransactionChainFailed(final DOMTransactionChain chain, final DOMDataTreeTransaction transaction,
            final Throwable cause) {
        LOG.error("Transaction chain for channel {} failed", channel, cause);
        channel.close();
    }

    @Override
    public void onTransactionChainSuccessful(final DOMTransactionChain chain) {
        LOG.info("Transaction chain for channel {} completed", channel);
    }
}
