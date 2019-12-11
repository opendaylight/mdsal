/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMDataBroker;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMTestKit.DOMListenerClassifier;

final class CapturingDOMDataBrokerImpl<D extends DOMDataBroker> extends ForwardingDOMDataBroker
        implements CapturingDOMDataBroker {
    private final @NonNull D delegate;
    private final @Nullable FilteringDOMDataTreeChangeService dtcs;

    CapturingDOMDataBrokerImpl(final D delegate, final DOMListenerClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        final DOMDataTreeChangeService delegateDtcs = delegate.getExtensions()
                .getInstance(DOMDataTreeChangeService.class);
        this.dtcs = delegateDtcs == null ? null : new FilteringDOMDataTreeChangeService(delegateDtcs, classifier);
    }

    @Override
    public ImmutableClassToInstanceMap<DOMDataBrokerExtension> getExtensions() {
        // Local temporary to keep Eclipse happy
        final DOMDataTreeChangeService local = dtcs;
        return local == null ? ImmutableClassToInstanceMap.of()
                : ImmutableClassToInstanceMap.of(DOMDataTreeChangeService.class, local);
    }

    @Override
    public void fireCapturedEvents(final EventQueue.EventPredicate predicate) {
        checkState(dtcs != null, "Attempted to filter events without a DataTreeChangeService");
        dtcs.fireCapturedEvents(predicate);
    }

    @Override
    protected D delegate() {
        return delegate;
    }
}