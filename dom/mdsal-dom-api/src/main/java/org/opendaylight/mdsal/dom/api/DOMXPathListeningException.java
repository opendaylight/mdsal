/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

/**
 * Base exception for various causes why and {@link DOMXPathListener}
 * may be terminated by the {@link DOMXPathListenerService} implementation.
 */
public class DOMXPathListeningException extends Exception {
    private static final long serialVersionUID = 1L;

    public DOMXPathListeningException(final String message) {
        super(message);
    }

    public DOMXPathListeningException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
