/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * Utility class which implements {@link DOMRpcResult} by forwarding all methods
 * to a backing instance.
 */
@NonNullByDefault
public abstract class ForwardingDOMRpcResult extends ForwardingObject implements DOMRpcResult {
    @Override
    protected abstract DOMRpcResult delegate();

    @Override
    public Collection<? extends RpcError> getErrors() {
        return delegate().getErrors();
    }

    @Override
    public @Nullable ContainerNode getResult() {
        return delegate().getResult();
    }
}
