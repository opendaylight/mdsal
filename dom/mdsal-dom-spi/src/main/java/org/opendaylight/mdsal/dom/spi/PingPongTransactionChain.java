/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Function;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link DOMTransactionChain}, which has a very specific behavior, which some users may find
 * surprising. If keeps the general intent of the contract, but it makes sure there are never more than two transactions
 * allocated at any given time: one of them is being committed, and while that is happening, the other one acts as
 * a scratch pad. Once the committing transaction completes successfully, the scratch transaction is enqueued as soon as
 * it is ready.
 *
 * <p>
 * This mode of operation means that there is no inherent isolation between the front-end transactions and transactions
 * cannot be reasonably cancelled.
 *
 * <p>
 * It furthermore means that the transactions returned by {@link #newReadOnlyTransaction()} counts as an outstanding
 * transaction and the user may not allocate multiple read-only transactions at the same time.
 */
public final class PingPongTransactionChain extends AbstractPingPongTransactionChain {
    @SuppressFBWarnings(value = "SLF4J_LOGGER_SHOULD_BE_PRIVATE", justification = "API stability")
    static final Logger LOG = LoggerFactory.getLogger(PingPongTransactionChain.class);

    public PingPongTransactionChain(final Function<DOMTransactionChainListener, DOMTransactionChain> delegateFactory,
            final DOMTransactionChainListener listener) {
        super(delegateFactory, listener);
    }
}
