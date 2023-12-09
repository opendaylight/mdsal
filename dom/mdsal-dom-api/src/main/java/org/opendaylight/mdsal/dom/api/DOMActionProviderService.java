/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A {@link DOMService} which allows registration of action implementations with a conceptual router. The client
 * counterpart of this service is {@link DOMActionService}.
 */
@NonNullByDefault
public interface DOMActionProviderService
        extends DOMService<DOMActionProviderService, DOMActionProviderService.Extension> {
    /**
     * Marker interface for extensions of {@link DOMActionProviderService}.
     */
    interface Extension extends DOMService.Extension<DOMActionProviderService, Extension> {
        // Marker interface
    }

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instances.
     *
     * @param implementation action implementation, must not be null
     * @param instances Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link Registration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code instances} is null, or if {@code instances}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    Registration registerActionImplementation(DOMActionImplementation implementation, Set<DOMActionInstance> instances);

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instance.
     *
     * @param implementation action implementation, must not be null
     * @param instance supported operation identifier. Must not be null.
     * @return A {@link Registration} object, guaranteed to be non-null.
     * @throws NullPointerException if any argument is null
     */
    default Registration registerActionImplementation(final DOMActionImplementation implementation,
            final DOMActionInstance instance) {
        return registerActionImplementation(implementation, ImmutableSet.of(instance));
    }

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instances.
     *
     * @param implementation action implementation, must not be null
     * @param instances Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link Registration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code instances} is null, or if {@code instances}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    default Registration registerActionImplementation(final DOMActionImplementation implementation,
            final DOMActionInstance... instances) {
        return registerActionImplementation(implementation, ImmutableSet.copyOf(instances));
    }
}
