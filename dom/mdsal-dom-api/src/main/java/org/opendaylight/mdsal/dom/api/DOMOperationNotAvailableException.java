/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception indicating that no implementation of the requested operation is available.
 */
@Beta
@NonNullByDefault
public final class DOMOperationNotAvailableException extends DOMOperationException {
    private static final long serialVersionUID = 1L;

    public DOMOperationNotAvailableException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    public DOMOperationNotAvailableException(final Throwable cause, final String format, final Object... args) {
        super(String.format(format, args), requireNonNull(cause));
    }
}
