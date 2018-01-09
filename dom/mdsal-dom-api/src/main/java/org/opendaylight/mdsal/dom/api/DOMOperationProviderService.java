/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A {@link DOMService} which allows registration of operation implementations with a conceptual
 * router. The client counterpart of this service is {@link DOMOperationService}.
 */
public interface DOMOperationProviderService extends DOMService {
    /**
     * Register an {@link DOMOperationImplementation} object with this service.
     *
     * @param implementation operation implementation, must not be null
     * @param operations Array of supported operation identifiers.
     *             Must not be null, empty, or contain a null element.
     *             Each identifier is added exactly once, no matter how many times it occurs.
     * @return A {@link DOMOperationImplementationRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty or contains a null element.
     */
    @Nonnull <T extends DOMOperationImplementation> DOMOperationImplementationRegistration<T>
        registerOperationImplementation(@Nonnull T implementation, @Nonnull DOMRpcIdentifier... operations);

    /**
     * Register an {@link DOMOperationImplementation} object with this service.
     *
     * @param implementation operation implementation, must not be null
     * @param operations Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link DOMOperationImplementationRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if types is empty or contains a null element.
     */
    @Nonnull <T extends DOMOperationImplementation> DOMOperationImplementationRegistration<T>
        registerOperationImplementation(@Nonnull T implementation, @Nonnull Set<DOMRpcIdentifier> operations);
}
