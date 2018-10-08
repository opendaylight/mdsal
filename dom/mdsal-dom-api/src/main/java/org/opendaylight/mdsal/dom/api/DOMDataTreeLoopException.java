/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Exception thrown when a loop is detected in the way {@link DOMDataTreeListener}
 * and {@link DOMDataTreeProducer} instances would be connected.
 */
public class DOMDataTreeLoopException extends Exception {
    private static final long serialVersionUID = 1L;

    public DOMDataTreeLoopException(@NonNull final String message) {
        super(message);
    }

    public DOMDataTreeLoopException(@NonNull final String message, @NonNull final Throwable cause) {
        super(message, cause);
    }
}
