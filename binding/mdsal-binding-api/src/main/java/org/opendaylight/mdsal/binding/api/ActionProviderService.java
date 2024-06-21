/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Registration interface used by {@code action} implementations. Each action is defined in a YANG model,
 * and implementations can be invoked  dynamically at runtime, via {@link ActionService}. Implementations registered
 * with this interface may throw {@link IllegalArgumentException}s when they encounter inconsistent input data and
 * {@link IllegalStateException} in they are unable to service the request.
 */
public interface ActionProviderService extends BindingService {
    /**
     * Register an implementation of an action, potentially constrained to a set of nodes.
     *
     * @param spec Action instance specification
     * @param implementation Implementation of {@code actionInterface}
     * @param datastore {@link LogicalDatastoreType} on which the implementation operates
     * @param validNodes Set of nodes this implementation is constrained to, empty if this implementation can handle
     *                   any target node.
     * @return A {@link Registration}
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if any of the {@code validNodes} does not match {@code datastore}
     * @throws UnsupportedOperationException if this service cannot handle requested datastore
     */
    <P extends DataObject, A extends Action<? extends InstanceIdentifier<P>, ?, ?>>
        @NonNull Registration registerImplementation(@NonNull ActionSpec<A, P> spec, @NonNull A implementation,
            @NonNull LogicalDatastoreType datastore, @NonNull Set<? extends InstanceIdentifier<P>> validNodes);

    default <P extends DataObject, A extends Action<? extends InstanceIdentifier<P>, ?, ?>>
        @NonNull Registration registerImplementation(final @NonNull ActionSpec<A, P> spec,
            final @NonNull A implementation, final @NonNull LogicalDatastoreType datastore) {
        return registerImplementation(spec, implementation, datastore, ImmutableSet.of());
    }

    default <P extends DataObject, A extends Action<? extends InstanceIdentifier<P>, ?, ?>>
            @NonNull Registration registerImplementation(final @NonNull ActionSpec<A, P> spec,
                final @NonNull A implementation) {
        return registerImplementation(spec, implementation, LogicalDatastoreType.OPERATIONAL);
    }
}
