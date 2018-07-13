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
import java.util.Collection;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMOperationResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
@NonNullByDefault
@ThreadSafe
public final class SimpleDOMOperationResult implements DOMOperationResult, Immutable {
    private final Collection<RpcError> errors;
    private final @Nullable ContainerNode output;

    private SimpleDOMOperationResult(final Collection<RpcError> errors, final @Nullable ContainerNode output) {
        this.errors = ImmutableList.copyOf(errors);
        this.output = null;
    }

    public SimpleDOMOperationResult(final Collection<RpcError> errors) {
        this(errors, null);
    }

    public SimpleDOMOperationResult(final ContainerNode output, final Collection<RpcError> errors) {
        this(errors, requireNonNull(output));
    }

    @Override
    public Collection<RpcError> getErrors() {
        return errors;
    }

    @Override
    public Optional<@Nullable ContainerNode> getOutput() {
        return Optional.ofNullable(output);
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