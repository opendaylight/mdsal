/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A {@link DOMService} which allows registration of RPC implementations with a conceptual router. The client
 * counterpart of this service is {@link DOMRpcService}.
 */
public interface DOMRpcProviderService extends DOMService<DOMRpcProviderService, DOMRpcProviderService.Extension> {
    /**
     * Marker interface for an extension to {@link DOMRpcProviderService}.
     */
    interface Extension extends DOMService.Extension<DOMRpcProviderService, Extension> {
        // Marker interface
    }

    /**
     * Register an {@link DOMRpcImplementation} object with this service.
     *
     * @param implementation RPC implementation, must not be null
     * @param rpcs Array of supported RPC identifiers. Must not be null, empty, or contain a null element.
     *             Each identifier is added exactly once, no matter how many times it occurs.
     * @return A {@link Registration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty or contains a null element.
     */
    default @NonNull Registration registerRpcImplementation(final @NonNull DOMRpcImplementation implementation,
            final @NonNull DOMRpcIdentifier... rpcs) {
        return registerRpcImplementation(implementation, Set.of(rpcs));
    }

    /**
     * Register an {@link DOMRpcImplementation} object with this service.
     *
     * @param implementation RPC implementation, must not be null
     * @param rpcs Set of supported RPC identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link Registration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty or contains a null element.
     */
    // FIXME: just Registration and forward to Map
    @NonNull Registration registerRpcImplementation(@NonNull DOMRpcImplementation implementation,
        @NonNull Set<DOMRpcIdentifier> rpcs);

    /**
     * Register a set of {@link DOMRpcImplementation}s with this service. The registration is performed atomically.
     *
     * @param map Map of RPC identifiers and their corresponding implementations
     * @return A registration object, guaranteed to be non-null
     * @throws NullPointerException if map is null or contains a null element
     * @throws IllegalArgumentException if map is empty.
     */
    @NonNull Registration registerRpcImplementations(Map<DOMRpcIdentifier, DOMRpcImplementation> map);
}
