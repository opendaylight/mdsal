/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for failures that can occur during operation invocation. This covers transport and protocol-level
 * failures, not implementation-reported errors, which are part of {@link DOMRpcResult}.
 */
// FIXME: remove this exception and rename DOMRpcException to DOMOperationException
@Beta
@Deprecated
@NonNullByDefault
public abstract class DOMActionException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construct an new instance with a message and an empty cause.
     *
     * @param message Exception message
     */
    protected DOMActionException(final String message) {
        super(message);
    }

    /**
     * Construct an new instance with a message and a cause.
     *
     * @param message Exception message
     * @param cause Chained cause
     */
    protected DOMActionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
