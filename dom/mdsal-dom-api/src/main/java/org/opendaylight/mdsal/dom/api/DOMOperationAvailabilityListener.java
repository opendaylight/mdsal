/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link EventListener} used to track Operation implementations becoming (un)available
 * to a {@link DOMOperationService}.
 */
@NonNullByDefault
public interface DOMOperationAvailabilityListener extends EventListener {
    /**
     * Method invoked whenever an operation type becomes available.
     *
     * @param operations operation types newly available
     */
    void onOperationAvailable(Collection<DOMRpcIdentifier> operations);

    /**
     * Method invoked whenever an operation type becomes unavailable.
     *
     * @param operations operation types which became unavailable
     */
    void onOperationUnavailable(Collection<DOMRpcIdentifier> operations);

    /**
     * Implementation filtering method. This method is useful for forwarding operation implementations,
     * which need to ensure they do not re-announce their own implementations. Without this method
     * a forwarder which registers an implementation would be notified of its own implementation,
     * potentially re-exporting it as local -- hence creating a forwarding loop.
     *
     * @param impl RPC implementation being registered
     * @return False if the implementation should not be reported, defaults to true.
     */
    default boolean acceptsImplementation(final DOMOperationImplementation impl) {
        return true;
    }
}
