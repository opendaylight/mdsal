/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcInput;

/**
 * Provides access to registered {@code action} implementations. Each action is defined in a YANG model,
 * and implementations are added dynamically at runtime, via {@link ActionProviderService}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface ActionService extends BindingService {
    /**
     * Returns an implementation of a requested {@link Action}. Returned instance is not an actual implementation
     * of the Action service interface, but a proxy implementation of the interface that forwards to an actual
     * implementation, if any.
     *
     * <p>
     * The following describes the behavior of the proxy when invoking
     * {@link Action#invoke(InstanceIdentifier, RpcInput)}:
     * <ul>
     * <li>If an actual implementation is registered with the MD-SAL, all invocations are forwarded to the registered
     * implementation.</li>
     * <li>If no actual implementation is registered, all invocations will fail by throwing
     * {@link IllegalStateException}.</li>
     * <li>Prior to invoking the actual implementation, the method arguments are are validated. If any are invalid,
     * an {@link IllegalArgumentException} is thrown.
     * </ul>
     *
     * <p>
     * The returned proxy is automatically updated with the most recent registered implementation, hence there is no
     * guarantee that multiple consecutive invocations will be handled by the same implementation.
     *
     * @param actionInterface Generated Action interface
     * @param validNodes Set of nodes this service will be constrained to, empty if no constraints are known
     * @return A proxy implementation of the generated interface
     * @throws NullPointerException if {@code actionInterface} is null
     * @throws IllegalArgumentException when {@code actionInterface} does not conform to the Binding Specification
     */
    <O extends DataObject, T extends Action<O, ?, ?>> T getActionHandle(Class<T> actionInterface,
            Set<DataTreeIdentifier<O>> validNodes);

    default <O extends DataObject, T extends Action<O, ?, ?>> T getActionHandle(final Class<T> actionInterface) {
        return getActionHandle(actionInterface, ImmutableSet.of());
    }

    default <O extends DataObject, T extends Action<O, ?, ?>> T getActionHandle(final Class<T> actionInterface,
            final LogicalDatastoreType dataStore, final InstanceIdentifier<O> path) {
        return getActionHandle(actionInterface, ImmutableSet.of(DataTreeIdentifier.create(dataStore, path)));
    }

    default <O extends DataObject, T extends Action<O, ?, ?>> T getActionHandle(final Class<T> actionInterface,
            final InstanceIdentifier<O> path) {
        return getActionHandle(actionInterface, LogicalDatastoreType.OPERATIONAL, path);
    }

    default <O extends DataObject, T extends Action<O, ?, ?>> T getActionHandle(final Class<T> actionInterface,
            @SuppressWarnings("unchecked") final DataTreeIdentifier<O>... nodes) {
        return getActionHandle(actionInterface, ImmutableSet.copyOf(nodes));
    }
}
