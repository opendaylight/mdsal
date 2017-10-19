/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * A {@link DOMService} which allows registration of operation implementations with a conceptual
 * router. The client counterpart of this service is {@link DOMOperationService}.
 */
@Beta
@NonNullByDefault
public interface DOMOperationProviderService
        extends DOMExtensibleService<DOMOperationProviderService, DOMOperationProviderServiceExtension> {
    /**
     * Register an {@link DOMOperationImplementation} object with this service.
     *
     * @param implementation operation implementation, must not be null
     * @param operations Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty
     */
    <T extends DOMOperationImplementation> ObjectRegistration<T> registerOperationImplementation(T implementation,
            Set<DOMRpcIdentifier> operations);

    /**
     * Register an {@link DOMOperationImplementation} object with this service.
     *
     * @param implementation operation implementation
     * @param operations Array of supported operation identifiers.
     *             Must not be null, empty, or contain a null element.
     *             Each identifier is added exactly once, no matter how many times it occurs.
     * @return A {@link ObjectRegistration} object.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty
     */
    default <T extends DOMOperationImplementation> ObjectRegistration<T> registerOperationImplementation(
            final T implementation, final DOMRpcIdentifier... operations) {
        return registerOperationImplementation(implementation, ImmutableSet.copyOf(operations));
    }
}
