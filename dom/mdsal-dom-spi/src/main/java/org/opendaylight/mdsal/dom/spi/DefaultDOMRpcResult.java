/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * Utility class implementing {@link DefaultDOMRpcResult}.
 */
@Beta
@NonNullByDefault
public final class DefaultDOMRpcResult implements DOMRpcResult, Immutable, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Interfaces do not specify Serializable")
    private final @Nullable ContainerNode value;
    // FIXME: a plain Collection is bad for equality
    private final Collection<? extends RpcError> errors;

    public DefaultDOMRpcResult(final RpcError error) {
        this(null, List.of(error));
    }

    public DefaultDOMRpcResult(final RpcError... errors) {
        this(null, List.of(errors));
    }

    public DefaultDOMRpcResult(final @Nullable ContainerNode result) {
        this(result, List.of());
    }

    public DefaultDOMRpcResult(final ContainerNode value, final RpcError error) {
        this(value, List.of(error));
    }

    public DefaultDOMRpcResult(final ContainerNode value, final RpcError... errors) {
        this(value, List.of(errors));
    }

    public DefaultDOMRpcResult(final @Nullable ContainerNode result, final Collection<? extends RpcError> errors) {
        value = result;
        this.errors = requireNonNull(errors);
    }

    public DefaultDOMRpcResult(final Collection<RpcError> errors) {
        this(null, errors);
    }

    // As per RFC7950 page 80 (top)
    public static DOMRpcResult ofMalformedMessage(final Exception cause) {
        return new DefaultDOMRpcResult(RpcResultBuilder.newError(ErrorType.RPC, ErrorTag.MALFORMED_MESSAGE,
            cause.getMessage(), null, null, requireNonNull(cause)));
    }

    @Override
    public Collection<? extends RpcError> errors() {
        return errors;
    }

    @Override
    public @Nullable ContainerNode value() {
        return value;
    }

    @Override
    public int hashCode() {
        int ret = errors.hashCode();
        final var local = value;
        if (local != null) {
            ret = 31 * ret + local.hashCode();
        }
        return ret;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof DefaultDOMRpcResult other && errors.equals(other.errors)
            && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("value", value)
            .add("errors", errors.isEmpty() ? null : errors)
            .toString();
    }
}
