/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base exception for various causes why and {@link DataTreeListener} may be terminated by the {@link DataTreeService}
 * implementation.
 */
public class DataTreeListeningException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataTreeListeningException(final @NonNull String message, final @Nullable Throwable cause) {
        super(requireNonNull(message, "message"), cause);
    }

    public DataTreeListeningException(@NonNull final String message) {
        super(requireNonNull(message, "message"));
    }
}