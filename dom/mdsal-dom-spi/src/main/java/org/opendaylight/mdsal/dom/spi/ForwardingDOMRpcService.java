/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcFuture;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * Utility {@link DOMRpcService} which forwards all requests to a backing delegate instance.
 */
public abstract class ForwardingDOMRpcService extends ForwardingDOMService<DOMRpcService, DOMRpcService.Extension>
        implements DOMRpcService {
    @Override
    protected abstract @NonNull DOMRpcService delegate();

    @Override
    public DOMRpcFuture invokeRpc(final QName type, final ContainerNode input) {
        return delegate().invokeRpc(type, input);
    }

    @Override
    public Registration registerRpcListener(final DOMRpcAvailabilityListener listener) {
        return delegate().registerRpcListener(listener);
    }
}
