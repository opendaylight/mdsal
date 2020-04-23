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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Utility class implementing {@link DefaultDOMRpcResult}.
 */
@Beta
@NonNullByDefault
public final class DefaultDOMRpcResult implements DOMRpcResult, Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Interfaces do not specify Serializable")
    private final @Nullable NormalizedNode<?, ?> result;
    private final Collection<? extends RpcError> errors;

    public DefaultDOMRpcResult(final NormalizedNode<?, ?> result, final RpcError... errors) {
        this(result, asCollection(errors));
    }

    public DefaultDOMRpcResult(final RpcError... errors) {
        this(null, asCollection(errors));
    }

    public DefaultDOMRpcResult(final @Nullable NormalizedNode<?, ?> result) {
        this(result, Collections.emptyList());
    }

    public DefaultDOMRpcResult(final @Nullable NormalizedNode<?, ?> result,
            final Collection<? extends RpcError> errors) {
        this.result = result;
        this.errors = requireNonNull(errors);
    }

    public DefaultDOMRpcResult(final Collection<RpcError> errors) {
        this(null, errors);
    }

    private static Collection<RpcError> asCollection(final RpcError... errors) {
        return errors.length == 0 ? Collections.emptyList() : Arrays.asList(errors);
    }

    @Override
    public Collection<? extends RpcError> getErrors() {
        return errors;
    }

    @Override
    public @Nullable NormalizedNode<?, ?> getResult() {
        return result;
    }

    @Override
    public int hashCode() {
        int ret = errors.hashCode();
        if (result != null) {
            ret = 31 * ret + result.hashCode();
        }
        return ret;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultDOMRpcResult)) {
            return false;
        }
        final DefaultDOMRpcResult other = (DefaultDOMRpcResult) obj;
        return errors.equals(other.errors) && Objects.equals(result, other.result);
    }
}
