/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Exception thrown when a loop is detected in the way {@link DataTreeListener} and {@link DataTreeProducer} instances
 * would be connected.
 */
public class DataTreeLoopException extends DataTreeProducerException {
    private static final long serialVersionUID = 1L;

    public DataTreeLoopException(final @NonNull String message, final @NonNull Throwable cause) {
        super(message, cause);
    }

    public DataTreeLoopException(final @NonNull String message) {
        super(message);
    }

}
