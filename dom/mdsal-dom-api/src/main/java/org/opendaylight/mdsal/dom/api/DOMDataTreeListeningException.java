/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.mdsal.common.api.ReadFailedException;

/**
 * Base exception for various causes why and {@link DOMDataTreeListener}
 * may be terminated by the {@link DOMDataTreeService} implementation.
 *
 * @deprecated This interface is scheduled for removal in the next major release.
 */
@Deprecated(forRemoval = true)
public class DOMDataTreeListeningException extends ReadFailedException {
    private static final long serialVersionUID = 1L;

    public DOMDataTreeListeningException(final String message) {
        super(message);
    }

    public DOMDataTreeListeningException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
