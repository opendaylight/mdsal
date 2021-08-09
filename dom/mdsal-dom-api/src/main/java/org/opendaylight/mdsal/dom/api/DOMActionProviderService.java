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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * A {@link DOMService} which allows registration of action implementations with a conceptual router. The client
 * counterpart of this service is {@link DOMActionService}.
 *
 * <p>
 * Implementations can be registered to specific {@link DOMActionInstance}s, where the implementation will be invoked
 * only for requests matching that instances. Alternatively an implementation can be registered to service all
 * {@link DOMActionInstance}s on specified {@link LogicalDatastoreType}. Registrations to specific instances take
 * precedence over registrations to all instances on a datastore.
 */
@Beta
@NonNullByDefault
public interface DOMActionProviderService
        extends DOMExtensibleService<DOMActionProviderService, DOMActionProviderServiceExtension> {
    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instances.
     *
     * @param implementation action implementation, must not be null
     * @param instances Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code instances} is null, or if {@code instances}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(T implementation,
        Set<DOMActionInstance> instances);

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instance.
     *
     * @param implementation action implementation, must not be null
     * @param instance supported operation identifier. Must not be null.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if any argument is null
     */
    default <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final DOMActionInstance instance) {
        return registerActionImplementation(implementation, ImmutableSet.of(instance));
    }

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing specified action instances.
     *
     * @param implementation action implementation, must not be null
     * @param instances Set of supported operation identifiers. Must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code instances} is null, or if {@code instances}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    default <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final DOMActionInstance... instances) {
        return registerActionImplementation(implementation, ImmutableSet.copyOf(instances));
    }

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing all action instances on specified
     * datastores.
     *
     * @param implementation action implementation, must not be null
     * @param type action type, must not be null
     * @param datastores Set of supported datastores, must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code datastores} is null, or if {@code datastores}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(T implementation,
        Absolute type, Set<LogicalDatastoreType> datastores);

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing all action instances on specified
     * datastore.
     *
     * @param implementation action implementation, must not be null
     * @param type action type, must not be null
     * @param datastore datastore to service
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if any argument is null
     */
    default <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final Absolute type, final LogicalDatastoreType datastore) {
        return registerActionImplementation(implementation, type, ImmutableSet.of(datastore));
    }

    /**
     * Register an {@link DOMActionImplementation} object with this service, servicing all action instances on specified
     * datastores.
     *
     * @param implementation action implementation, must not be null
     * @param type action type, must not be null
     * @param datastores Set of supported datastores, must not be null, empty, or contain a null element.
     * @return A {@link ObjectRegistration} object, guaranteed to be non-null.
     * @throws NullPointerException if {@code implementation} or {@code datastores} is null, or if {@code datastores}
     *                              contains a null element.
     * @throws IllegalArgumentException if {@code instances} is empty
     */
    default <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final Absolute type, final LogicalDatastoreType... datastores) {
        return registerActionImplementation(implementation, type, ImmutableSet.copyOf(datastores));
    }
}
