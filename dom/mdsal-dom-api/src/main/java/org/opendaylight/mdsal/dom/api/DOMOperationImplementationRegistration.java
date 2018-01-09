/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * A registration of a {@link DOMOperationImplementation}. Used to track and revoke a registration
 * with a {@link DOMOperationProviderService}.
 *
 * @param <T> RPC implementation type
 */
public interface DOMOperationImplementationRegistration<T extends DOMOperationImplementation>
        extends ObjectRegistration<T> {
    @Override
    void close();
}
