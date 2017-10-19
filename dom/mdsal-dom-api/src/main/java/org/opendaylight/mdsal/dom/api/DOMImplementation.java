/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;

/**
 * Super interface of old <code>DOMRpcImplementaion</code> and new <code>DOMOperationImplementaion</code>.
 * It would be eliminated when <code>DOMRpcImplementaion</code> is deprecated.
 *
 * @deprecated Do not use this interface directly.
 */
@Deprecated
@Beta
public interface DOMImplementation {
    /**
     * Return the relative invocation cost of this implementation. Default implementation return 0.
     *
     * @return Non-negative cost of invoking this implementation.
     */
    default long invocationCost() {
        return 0;
    }
}
