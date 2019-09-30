/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
@NonNullByDefault
@ThreadSafe
public final class SimpleDOMActionResult implements DOMActionResult, Immutable {
    private final Collection<RpcError> errors;
    private final @Nullable ContainerNode output;

    private SimpleDOMActionResult(final Collection<RpcError> errors, final @Nullable ContainerNode output) {
        this.errors = ImmutableList.copyOf(errors);
        this.output = output;
    }

    public SimpleDOMActionResult(final ContainerNode output) {
        this.errors = ImmutableList.of();
        this.output = requireNonNull(output);
    }

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public SimpleDOMActionResult(final Collection<RpcError> errors) {
        this(errors, null);
    }

    public SimpleDOMActionResult(final ContainerNode output, final Collection<RpcError> errors) {
        this(errors, requireNonNull(output));
    }

    // As per RFC7950 page 80 (top)
    public static SimpleDOMActionResult ofMalformedMessage(final Exception cause) {
        return new SimpleDOMActionResult(ImmutableList.of(RpcResultBuilder.newError(ErrorType.RPC, "malformed-message",
            cause.getMessage(), null, null, requireNonNull(cause))), null);
    }

    @Override
    public Collection<RpcError> getErrors() {
        return errors;
    }

    @Override
    public Optional<ContainerNode> getOutput() {
        return (Optional) Optional.<@Nullable ContainerNode>ofNullable(output);
    }

    @Override
    public String toString() {
        final ToStringHelper helper = MoreObjects.toStringHelper(this).omitNullValues().add("output", output);
        if (!errors.isEmpty()) {
            helper.add("errors", errors);
        }
        return helper.toString();
    }
}
