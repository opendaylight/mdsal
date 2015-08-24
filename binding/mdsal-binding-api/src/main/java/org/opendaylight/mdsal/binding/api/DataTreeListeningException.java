/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base exception for various causes why and {@link DataTreeListener} may be terminated by the
 * {@link DataTreeService} implementation.
 */
public class DataTreeListeningException extends Exception {


    private static final long serialVersionUID = 1L;

    public DataTreeListeningException(@Nonnull String message, @Nullable Throwable cause) {
        super(Preconditions.checkNotNull(message, "message"), cause);
    }

    public DataTreeListeningException(@Nonnull String message) {
        super(Preconditions.checkNotNull(message, "message"));
    }
}