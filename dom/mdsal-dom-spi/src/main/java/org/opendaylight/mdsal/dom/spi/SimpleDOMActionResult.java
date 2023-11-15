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
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
public final class SimpleDOMActionResult implements DOMActionResult, Immutable {
    private final @NonNull Collection<RpcError> errors;
    private final @Nullable ContainerNode output;

    private SimpleDOMActionResult(final @NonNull Collection<RpcError> errors, final @Nullable ContainerNode output) {
        this.errors = ImmutableList.copyOf(errors);
        this.output = output;
    }

    public SimpleDOMActionResult(final @NonNull ContainerNode output) {
        errors = ImmutableList.of();
        this.output = requireNonNull(output);
    }

    public SimpleDOMActionResult(final @NonNull Collection<RpcError> errors) {
        this(errors, null);
    }

    public SimpleDOMActionResult(final @NonNull ContainerNode output, final @NonNull Collection<RpcError> errors) {
        this(errors, requireNonNull(output));
    }

    // As per RFC7950 page 80 (top)
    public static @NonNull SimpleDOMActionResult ofMalformedMessage(final @NonNull Exception cause) {
        return new SimpleDOMActionResult(ImmutableList.of(RpcResultBuilder.newError(ErrorType.RPC,
            ErrorTag.MALFORMED_MESSAGE, cause.getMessage(), null, null, requireNonNull(cause))), null);
    }

    @Override
    public Collection<RpcError> getErrors() {
        return errors;
    }

    @Override
    public Optional<ContainerNode> getOutput() {
        return Optional.ofNullable(output);
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(this).omitNullValues().add("output", output);
        if (!errors.isEmpty()) {
            helper.add("errors", errors);
        }
        return helper.toString();
    }
}
