/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Utility methods for converting {@link RpcResult} to/from {@link DOMOperationResult} and {@link DOMRpcResult}.
 */
@NonNullByDefault
final class RpcResultUtil {
    private RpcResultUtil() {

    }

    /**
     * DOMRpcResult does not have a notion of success, hence we have to reverse-engineer it by looking at reported
     * errors and checking whether they are just warnings.
     */
    static <T> RpcResult<T> rpcResultFromDOM(final Collection<RpcError> errors, final @Nullable T result) {
        return RpcResultBuilder.<T>status(errors.stream().noneMatch(err -> err.getSeverity() == ErrorSeverity.ERROR))
                .withResult(result).withRpcErrors(errors).build();
    }
}
