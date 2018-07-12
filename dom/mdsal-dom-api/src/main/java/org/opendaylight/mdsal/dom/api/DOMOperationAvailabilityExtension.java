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
import java.util.concurrent.Executor;
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
     * Register a {@link AvailabilityListener} with this service to receive notifications about operation
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link DOMOperationService#invokeRpc(QName, ContainerNode, DOMOperationCallback, Executor)} and
     * {@link DOMOperationService#invokeAction(SchemaPath, DOMDataTreeIdentifier, ContainerNode)} will not report
     * a failure due to {@link DOMOperationNotAvailableException} and need to be ready to handle it.
     *
     * <p>
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from occurring.
     *
     * @param listener {@link AvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it.
     * @throws NullPointerException if {@code listener} is null
     */
    <T extends AvailabilityListener> ListenerRegistration<T> registerAvailabilityListener(T listener);

    /**
     * An {@link EventListener} used to track Operation implementations becoming (un)available
     * to a {@link DOMOperationService}.
     */
    interface AvailabilityListener extends EventListener {
        /**
         * Method invoked whenever an operation type becomes available or unavailable. There are two sets reported,
         * removed and added. To reconstruct the state, first apply removed and then added operations, like this:
         *
         * <code>
         *     Set&lt;AvailableOperation&lt;?&gt;&gt; operations;
         *     operations.removeAll(removed);
         *     operations.addAll(added);
         * </code>
         *
         * @param removed operations which disappeared
         * @param added operations which became available
         */
        void onOperationsChanged(Set<DOMOperationInstance<?>> removed, Set<DOMOperationInstance<?>> added);

        /**
         * Implementation filtering method. This method is useful for forwarding operation implementations,
         * which need to ensure they do not re-announce their own implementations. Without this method
         * a forwarder which registers an implementation would be notified of its own implementation,
         * potentially re-exporting it as local -- hence creating a forwarding loop.
         *
         * @param impl Operation implementation being registered
         * @return False if the implementation should not be reported, defaults to true.
         */
        default boolean acceptsImplementation(final DOMOperationImplementation impl) {
            return true;
        }
    }
}
