/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when an {@link ImplementedModule} cannot be started.
 */
@Beta
// FIXME: sealed subclasses when we have JDK17+
public class ImplementationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ImplementationException(final String message) {
        super(requireNonNull(message));
    }

    public ImplementationException(final String message, final Throwable cause) {
        super(requireNonNull(message), requireNonNull(cause));
    }
}
