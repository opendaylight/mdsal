/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.EventListener;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
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
     * Register a {@link AvailabilityListener} with this service to receive notifications about operation
     * implementations becoming (un)available. The listener will be invoked with the current implementations reported
     * and will be kept uptodate as implementations come and go.
     *
     * <p>
     * Users should note that using a listener does not necessarily mean that
     * {@link DOMOperationService#invokeRpc(QName, NormalizedNode, DOMOperationCallback, Executor)} and
     * {@link DOMOperationService#invokeAction(SchemaPath, DOMDataTreeIdentifier, NormalizedNode)} will not report
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
        void onOperationsChanged(Set<AvailableOperation<?>> removed, Set<AvailableOperation<?>> added);

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

    /**
     * An operation (RPC or action) which is subject to availability. This is a common superclass for
     * {@link AvailableAction} and {@link AvailableRpc}.
     *
     * @param <T> type of operation type
     */
    abstract class AvailableOperation<T> implements Immutable {
        private final T type;

        AvailableOperation(final T type) {
            this.type = requireNonNull(type);
        }

        /**
         * Return the operation type.
         *
         * @return operation type.
         */
        public final T getType() {
            return type;
        }

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(@Nullable Object obj);

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("type", type);
        }
    }

    final class AvailableAction extends AvailableOperation<SchemaPath> {
        private final Set<DOMDataTreeIdentifier> dataTrees;

        public AvailableAction(final SchemaPath type, final Set<DOMDataTreeIdentifier> dataTrees) {
            super(type);
            this.dataTrees = ImmutableSet.copyOf(dataTrees);
            checkArgument(!dataTrees.isEmpty());
        }

        /**
         * Return the set of data trees on which this action is available. Note that
         * {@link DOMDataTreeIdentifier#getRootIdentifier()} may be a wildcard.
         *
         * @return Set of trees on which this action is available.
         */
        public Set<DOMDataTreeIdentifier> getDataTrees() {
            return dataTrees;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), dataTrees);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AvailableAction)) {
                return false;
            }
            final AvailableAction other = (AvailableAction) obj;
            return getType().equals(other.getType()) && dataTrees.equals(other.dataTrees);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("dataTrees", dataTrees);
        }
    }

    final class AvailableRpc extends AvailableOperation<QName> {
        public AvailableRpc(final QName type) {
            super(type);
        }

        @Override
        public int hashCode() {
            return getType().hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof AvailableRpc && getType().equals(((AvailableRpc) obj).getType());
        }
    }
}
