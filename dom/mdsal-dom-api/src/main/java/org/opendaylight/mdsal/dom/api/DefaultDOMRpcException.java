/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

/**
 * Default implementation of DOMRpcException.
 *
 * @author Thomas Pantelis
 */
public class DefaultDOMRpcException extends DOMRpcException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public DefaultDOMRpcException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
