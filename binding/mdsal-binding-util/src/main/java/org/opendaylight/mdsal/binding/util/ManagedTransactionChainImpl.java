/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.TransactionChain;

/**
 * Implementation of {@link ManagedTransactionChain}, based on {@link ManagedTransactionFactoryImpl}.
 */
class ManagedTransactionChainImpl extends ManagedTransactionFactoryImpl<TransactionChain>
        implements ManagedTransactionChain {
    ManagedTransactionChainImpl(final TransactionChain realTxChain) {
        super(realTxChain);
    }
}
