/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.common.OperationFailedException;
import org.opendaylight.yangtools.yang.common.RpcError;

/**
 * An exception for a failed read.
 */
public class ReadFailedException extends OperationFailedException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * An {@link ExceptionMapper} producing {@link ReadFailedException}s.
     *
     * @deprecated Use {@link #ofCaught(Throwable)} instead.
     */
    @Deprecated(since = "13.0.4", forRemoval = true)
    public static final ExceptionMapper<ReadFailedException> MAPPER =
        new ExceptionMapper<>("read", ReadFailedException.class) {
            @Override
            protected ReadFailedException newWithCause(final String message, final Throwable cause) {
                return new ReadFailedException(message, cause);
            }
        };

    public ReadFailedException(final String message, final RpcError... errors) {
        super(message, errors);
    }

    public ReadFailedException(final String message, final Throwable cause, final RpcError... errors) {
        super(message, cause, errors);
    }

    public ReadFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @NonNullByDefault
    public static ReadFailedException ofCaught(final Throwable caught) {
        return OperationFailedExceptionUtil.ofCaught(caught, ReadFailedException.class, "read",
            ReadFailedException::new);
    }
}
