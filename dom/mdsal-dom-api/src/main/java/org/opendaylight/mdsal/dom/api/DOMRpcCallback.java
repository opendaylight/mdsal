/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.FutureCallback;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link FutureCallback} tied to a {@link DOMRpcFuture}.
 */
public abstract class DOMRpcCallback implements FutureCallback<@NonNull DOMRpcResult> {
    @Override
    public final void onFailure(final Throwable cause) {
        onFailure(cause instanceof DOMRpcException dre ? dre : new DefaultDOMRpcException("Unexpected failure", cause));
    }

    /**
     * Invoked when the future completes with a failure.
     *
     * @param failure the failure
     */
    protected abstract void onFailure(@NonNull DOMRpcException failure);
}
