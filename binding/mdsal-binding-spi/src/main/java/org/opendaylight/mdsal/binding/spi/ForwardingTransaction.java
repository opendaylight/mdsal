/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spi;

import com.google.common.collect.ForwardingObject;
import org.opendaylight.mdsal.binding.api.Transaction;

/**
 * Specialization of {@link ForwardingObject} for a {@link Transaction}.
 */
public abstract class ForwardingTransaction extends ForwardingObject implements Transaction {
    @Override
    protected abstract Transaction delegate();

    @Override
    public Object getIdentifier() {
        return delegate().getIdentifier();
    }
}
