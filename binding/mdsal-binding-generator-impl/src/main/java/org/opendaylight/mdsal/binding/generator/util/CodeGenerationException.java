/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

@Deprecated
public class CodeGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CodeGenerationException() {
    }

    public CodeGenerationException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CodeGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CodeGenerationException(final String message) {
        super(message);
    }

    public CodeGenerationException(final Throwable cause) {
        super(cause);
    }
}
