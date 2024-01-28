/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ForwardingObject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcFuture;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * Utility implementation which implements {@link DOMRpcImplementation} by forwarding it to
 * a backing delegate.
 */
public abstract class ForwardingDOMRpcImplementation extends ForwardingObject implements DOMRpcImplementation {
    @Override
    protected abstract @NonNull DOMRpcImplementation delegate();

    @Override
    public DOMRpcFuture invokeRpc(final DOMRpcIdentifier type, final ContainerNode input) {
        return delegate().invokeRpc(type, input);
    }

    @Override
    public long invocationCost() {
        return delegate().invocationCost();
    }
}
