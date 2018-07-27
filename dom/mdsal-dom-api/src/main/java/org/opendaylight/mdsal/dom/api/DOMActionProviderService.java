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
 * A {@link DOMService} which allows registration of action implementations with a conceptual router. The client
 * counterpart of this service is {@link DOMActionService}.
 */
@Beta
@NonNullByDefault
public interface DOMActionProviderService
        extends DOMExtensibleService<DOMActionProviderService, DOMActionProviderServiceExtension> {
    /**
     * Register an {@link DOMActionImplementation} object with this service.
     *
     * @param implementation action implementation, must not be null
     * @param instances Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if implementation or types is null
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(T implementation,
            Set<DOMActionInstance> instances);

    default <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final DOMActionInstance... instances) {
        return registerActionImplementation(implementation, ImmutableSet.copyOf(instances));
    }
}
