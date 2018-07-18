/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import java.util.EventListener;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * An {@link DOMOperationServiceExtension} exposed by {@link DOMOperationService}s which allow their users to listen
 * for operations becoming available.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DOMOperationAvailabilityExtension extends DOMOperationServiceExtension {
    /**
     * Register a {@link RpcAvailabilityListener} with this service to receive notifications about Rpc
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link DOMOperationService#invokeRpc(QName, ContainerNode)} and
     * {@link DOMOperationService#invokeAction(SchemaPath, DOMDataTreeIdentifier, ContainerNode)} will not report
     * a failure due to {@link DOMOperationNotAvailableException} and need to be ready to handle it.
     *
     * <p>
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from occurring.
     *
     * @param listener {@link RpcAvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it.
     * @throws NullPointerException if {@code listener} is null
     */
    <T extends RpcAvailabilityListener> ListenerRegistration<T> registerRpcAvailabilityListener(T listener);

    /**
     * Register an {@link ActionAvailabilityListener} with this service to receive notifications about Action
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link DOMOperationService#invokeRpc(QName, ContainerNode)} and
     * {@link DOMOperationService#invokeAction(SchemaPath, DOMDataTreeIdentifier, ContainerNode)} will not report
     * a failure due to {@link DOMOperationNotAvailableException} and need to be ready to handle it.
     *
     * <p>
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from occurring.
     *
     * @param listener {@link RpcAvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it.
     * @throws NullPointerException if {@code listener} is null
     */
    <T extends ActionAvailabilityListener> ListenerRegistration<T> registerActionAvailabilityListener(T listener);

    /**
     * An {@link EventListener} used to track Rpc implementations becoming (un)available
     * to a {@link DOMOperationService}.
     */
    interface RpcAvailabilityListener extends EventListener {
        /**
         * Method invoked whenever a Rpc type becomes available or unavailable. There are two sets reported,
         * removed and added. To reconstruct the state, first apply removed and then added Rpcs, like this:
         *
         * <code>
         *     Set&lt;AvailableOperation&lt;?&gt;&gt; operations;
         *     operations.removeAll(removed);
         *     operations.addAll(added);
         * </code>
         *
         * @param removed Rpcs which disappeared
         * @param added Rpcs which became available
         */
        void onRpcsChanged(Set<DOMOperationInstance.Rpc> removed, Set<DOMOperationInstance.Rpc> added);

        /**
         * Implementation filtering method. This method is useful for forwarding Rpc implementations,
         * which need to ensure they do not re-announce their own implementations. Without this method
         * a forwarder which registers an implementation would be notified of its own implementation,
         * potentially re-exporting it as local -- hence creating a forwarding loop.
         *
         * @param impl Rpc implementation being registered
         * @return False if the implementation should not be reported, defaults to true.
         */
        default boolean acceptsRpcImplementation(final DOMOperationImplementation.Rpc impl) {
            return true;
        }
    }

    /**
     * An {@link EventListener} used to track Action implementations becoming (un)available
     * to a {@link DOMOperationService}.
     */
    interface ActionAvailabilityListener extends EventListener {
        /**
         * Method invoked whenever an Action type becomes available or unavailable. There are two sets reported,
         * removed and added. To reconstruct the state, first apply removed and then added Actionss, like this:
         *
         * <code>
         *     Set&lt;AvailableOperation&lt;?&gt;&gt; operations;
         *     operations.removeAll(removed);
         *     operations.addAll(added);
         * </code>
         *
         * @param removed Actions which disappeared
         * @param added Actions which became available
         */
        void onActionsChanged(Set<DOMOperationInstance.Action> removed, Set<DOMOperationInstance.Action> added);

        /**
         * Implementation filtering method. This method is useful for forwarding Action implementations,
         * which need to ensure they do not re-announce their own implementations. Without this method
         * a forwarder which registers an implementation would be notified of its own implementation,
         * potentially re-exporting it as local -- hence creating a forwarding loop.
         *
         * @param impl Action implementation being registered
         * @return False if the implementation should not be reported, defaults to true.
         */
        default boolean acceptsActionImplementation(final DOMOperationImplementation.Action impl) {
            return true;
        }
    }
}
