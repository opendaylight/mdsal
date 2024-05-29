/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.OperationFailedException;
import org.opendaylight.yangtools.yang.common.RpcError;

/**
 * Failed commit of asynchronous transaction. This exception is raised and returned when transaction commit failed.
 */
public class TransactionCommitFailedException extends OperationFailedException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public TransactionCommitFailedException(final String message, final RpcError... errors) {
        this(message, null, errors);
    }

    public TransactionCommitFailedException(final String message, final Throwable cause, final RpcError... errors) {
        super(message, cause, errors);
    }

    public TransactionCommitFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @NonNullByDefault
    public static TransactionCommitFailedException ofCaught(final Throwable caught, final String operationName) {
        return OperationFailedExceptionUtil.ofCaught(caught, TransactionCommitFailedException.class, operationName,
            TransactionCommitFailedException::new);
    }
}
