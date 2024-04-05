/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * Interface defining a result of an RPC call.
 */
// FIXME: rename to DOMOperationResult
@NonNullByDefault
public interface DOMRpcResult {
    /**
     * Returns a set of errors and warnings which occurred during processing the call.
     *
     * @return a Collection of {@link RpcError}, guaranteed to be non-null. In case no errors are reported, an empty
     *         collection is returned.
     */
    Collection<? extends RpcError> errors();

    /**
     * Returns the value result of the call or {code null} if no result is available.
     *
     * @return Invocation result, {@code null} if the operation has not produced a result. This might be the case if the
     *         operation does not produce a result, or if it failed.
     */
    @Nullable ContainerNode value();
}
