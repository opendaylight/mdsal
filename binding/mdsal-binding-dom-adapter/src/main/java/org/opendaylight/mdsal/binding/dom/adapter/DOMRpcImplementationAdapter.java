/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Adapter based on {@link Rpc} specialization.
 */
final class DOMRpcImplementationAdapter<T extends Rpc<?, ?>> extends AbstractDOMRpcImplementationAdapter<T> {
    DOMRpcImplementationAdapter(final AdapterContext adapterContext, final T delegate) {
        super(adapterContext, delegate);
    }

    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final NormalizedNode input) {
        // TODO Auto-generated method stub
        return null;
    }
}
