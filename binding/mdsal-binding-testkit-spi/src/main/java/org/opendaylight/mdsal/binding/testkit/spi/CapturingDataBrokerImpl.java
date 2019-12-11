/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit.spi;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.spi.ForwardingDataBroker;
import org.opendaylight.mdsal.binding.testkit.spi.AbstractBindingServiceTestKit.TreeChangeListenerClassifier;

final class CapturingDataBrokerImpl<D extends DataBroker> extends ForwardingDataBroker implements CapturingDataBroker {
    private final D delegate;

    CapturingDataBrokerImpl(final D delegate, final TreeChangeListenerClassifier classifier) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void fireCapturedEvents(final EventPredicate predicate) {
        // TODO Auto-generated method stub

    }

    @Override
    protected D delegate() {
        return delegate;
    }
}
