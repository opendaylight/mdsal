/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Failure of asynchronous transaction commit caused by invalid data. This exception is raised and returned when
 * a transaction commit failed, because other data submitted via transactions.
 *
 * <p>Clients usually are not able recover from this error condition by retrieving same transaction, since data
 * introduced by this transaction is invalid.
 */
public class DataValidationFailedException extends TransactionCommitFailedException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final HierarchicalIdentifier<?> path;
    private final Class<? extends HierarchicalIdentifier<?>> pathType;

    public <P extends HierarchicalIdentifier<P>> DataValidationFailedException(final Class<P> pathType, final P path,
            final String message, final Throwable cause) {
        super(message, cause, RpcResultBuilder.newError(ErrorType.APPLICATION, ErrorTag.INVALID_VALUE, message, null,
            path != null ? path.toString() : null, cause));
        this.pathType = requireNonNull(pathType, "path type must not be null");
        this.path = requireNonNull(path, "path must not be null.");
    }

    public <P extends HierarchicalIdentifier<P>> DataValidationFailedException(final Class<P> pathType, final P path,
            final String message) {
        this(pathType, path, message, null);
    }

    public final HierarchicalIdentifier<?> getPath() {
        return path;
    }

    public final Class<? extends HierarchicalIdentifier<?>> getPathType() {
        return pathType;
    }
}
