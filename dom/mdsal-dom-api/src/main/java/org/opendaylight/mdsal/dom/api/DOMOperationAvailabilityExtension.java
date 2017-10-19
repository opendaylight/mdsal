/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
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
     * An {@link EventListener} used to track Operation implementations becoming (un)available
     * to a {@link DOMOperationService}.
     */
    public interface AvailabilityListener extends EventListener {
        /**
         * Method invoked whenever an operation type becomes available.
         *
         * @param operations operation types newly available
         */
        void onOperationAvailable(Collection<DOMRpcIdentifier> operations);

        /**
         * Method invoked whenever an operation type becomes unavailable.
         *
         * @param operations operation types which became unavailable
         */
        void onOperationUnavailable(Collection<DOMRpcIdentifier> operations);

        /**
         * Implementation filtering method. This method is useful for forwarding operation implementations,
         * which need to ensure they do not re-announce their own implementations. Without this method
         * a forwarder which registers an implementation would be notified of its own implementation,
         * potentially re-exporting it as local -- hence creating a forwarding loop.
         *
         * @param impl RPC implementation being registered
         * @return False if the implementation should not be reported, defaults to true.
         */
        default boolean acceptsImplementation(final DOMOperationImplementation impl) {
            return true;
        }
    }

    /**
     * Register a {@link AvailabilityListener} with this service to receive notifications about operation
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link DOMOperationService#invokeRpc(QName, NormalizedNode, DOMOperationCallback, Executor)} and
     * {@link DOMOperationService#invokeAction(SchemaPath, DOMDataTreeIdentifier, NormalizedNode) } will not report
     * a failure due to {@link DOMRpcImplementationNotAvailableException} and need to be ready to handle it.
     *
     * <p>
     * Implementations are encouraged to take reasonable precautions to prevent this scenario from occurring.
     *
     * @param listener {@link AvailabilityListener} instance to register
     * @return A {@link ListenerRegistration} representing this registration. Performing a
     *         {@link ListenerRegistration#close()} will cancel it.
     * @throws NullPointerException if {@code listener} is null
     */
    <T extends AvailabilityListener> ListenerRegistration<T> registerAvailabilityListener(@Nonnull T listener);
}
