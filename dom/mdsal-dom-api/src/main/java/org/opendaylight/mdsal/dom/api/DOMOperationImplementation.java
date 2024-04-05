/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

/**
 * Common interface between {@link DOMActionImplementation} and {@link DOMRpcImplementation}.
 */
public sealed interface DOMOperationImplementation permits DOMActionImplementation, DOMRpcImplementation {
    /**
     * Return the relative invocation cost of this implementation. Default implementation returns 0.
     *
     * @return Non-negative cost of invoking this implementation.
     */
    default long invocationCost() {
        return 0;
    }
}
