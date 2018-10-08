/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;

public class DataTreeProducerException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataTreeProducerException(final @NonNull String message, final @NonNull Throwable cause) {
        super(message, cause);
    }

    public DataTreeProducerException(final @NonNull String message) {
        super(message);
    }

}
