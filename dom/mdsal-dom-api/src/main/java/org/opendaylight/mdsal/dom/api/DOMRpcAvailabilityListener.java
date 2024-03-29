/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A  listene} used to track RPC implementations becoming (un)available to a {@link DOMRpcService}.
 */
public interface DOMRpcAvailabilityListener {
    /**
     * Method invoked whenever an RPC type becomes available.
     *
     * @param rpcs RPC types newly available
     */
    void onRpcAvailable(@NonNull Collection<DOMRpcIdentifier> rpcs);

    /**
     * Method invoked whenever an RPC type becomes unavailable.
     *
     * @param rpcs RPC types which became unavailable
     */
    void onRpcUnavailable(@NonNull Collection<DOMRpcIdentifier> rpcs);

    /**
     * Implementation filtering method. This method is useful for forwarding RPC implementations,
     * which need to ensure they do not re-announce their own implementations. Without this method
     * a forwarder which registers an implementation would be notified of its own implementation,
     * potentially re-exporting it as local -- hence creating a forwarding loop.
     *
     * @param impl RPC implementation being registered
     * @return False if the implementation should not be reported, defaults to true.
     */
    default boolean acceptsImplementation(final DOMRpcImplementation impl) {
        return true;
    }
}
