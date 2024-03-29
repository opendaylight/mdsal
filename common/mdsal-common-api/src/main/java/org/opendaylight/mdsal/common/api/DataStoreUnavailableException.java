/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import java.io.Serial;

/**
 * This exception occurs if the datastore is temporarily unavailable. A retry of the transaction may succeed after
 * a period of time.
 */
public class DataStoreUnavailableException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public DataStoreUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
