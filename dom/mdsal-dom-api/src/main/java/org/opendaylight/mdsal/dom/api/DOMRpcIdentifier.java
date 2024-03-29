/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Identifier of a RPC context. This is an extension of the YANG RPC, which always has global context. It allows an RPC
 * to have a instance identifier attached, so that there can be multiple implementations bound to different contexts
 * concurrently.
 */
@NonNullByDefault
public abstract class DOMRpcIdentifier {
    @VisibleForTesting
    static final class Global extends DOMRpcIdentifier {
        private Global(final QName type) {
            super(type);
        }

        @Override
        public YangInstanceIdentifier getContextReference() {
            return YangInstanceIdentifier.of();
        }
    }

    @VisibleForTesting
    static final class Local extends DOMRpcIdentifier {
        private final YangInstanceIdentifier contextReference;

        private Local(final QName type, final YangInstanceIdentifier contextReference) {
            super(type);
            this.contextReference = requireNonNull(contextReference);
        }

        @Override
        public YangInstanceIdentifier getContextReference() {
            return contextReference;
        }
    }

    private final QName type;

    private DOMRpcIdentifier(final QName type) {
        this.type = requireNonNull(type);
    }

    /**
     * Create a global RPC identifier.
     *
     * @param type RPC type, schema node identifier of its definition, may not be null
     * @return A global RPC identifier, guaranteed to be non-null.
     */
    public static DOMRpcIdentifier create(final QName type) {
        return new Global(type);
    }

    /**
     * Create an RPC identifier with a particular context reference.
     *
     * @param type RPC type, schema node identifier of its definition, may not be null
     * @param contextReference Context reference, null means a global RPC identifier.
     * @return A global RPC identifier, guaranteed to be non-null.
     */
    public static DOMRpcIdentifier create(final QName type, final @Nullable YangInstanceIdentifier contextReference) {
        if (contextReference == null || contextReference.isEmpty()) {
            return new Global(type);
        }

        return new Local(type, contextReference);
    }

    /**
     * Return the RPC type.
     *
     * @return RPC type.
     */
    public final QName getType() {
        return type;
    }

    /**
     * Return the RPC context reference. Null value indicates global context.
     *
     * @return RPC context reference.
     */
    public abstract YangInstanceIdentifier getContextReference();

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + Objects.hashCode(getContextReference());
        return result;
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DOMRpcIdentifier other && type.equals(other.type)
            && Objects.equals(getContextReference(), other.getContextReference());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("type", type)
            .add("contextReference", getContextReference())
            .toString();
    }
}
