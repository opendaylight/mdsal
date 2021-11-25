/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;

/**
 * A combination of an {@link Action} class and its corresponding instantiation wildcard, expressed as
 * an {@link InstanceIdentifier}. This glue is required because action interfaces are generated at the place of their
 * definition, most importantly in {@code grouping} and we actually need to bind to a particular instantiation (e.g. a
 * place where {@code uses} references that grouping).
 *
 * @param <A> Generated Action interface type
 * @param <P> Action parent path type
 */
@Beta
public final class ActionInstance<A extends Action<P, ?, ?>, P extends InstanceIdentifier<?>> {
    private final @NonNull Class<A> type;
    private final @NonNull P path;

    private ActionInstance(final Class<A> type, final P path) {
        this.type = requireNonNull(type);
        this.path = ensureWildcard(requireNonNull(path));
    }

    public static <A extends Action<P, ?, ?>, P extends InstanceIdentifier<?>> @NonNull ActionInstance<A, P> of(
            final Class<A> type, final P path) {
        return new ActionInstance<>(type, path);
    }

    public @NonNull P path() {
        return path;
    }

    public @NonNull Class<A> type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ActionInstance)) {
            return false;
        }
        final var other = (ActionInstance<?, ?>) obj;
        return type.equals(other.type) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", type).add("path", path).toString();
    }

    private static <P extends InstanceIdentifier<?>> @NonNull P ensureWildcard(final @NonNull P path) {
        for (var item : path.getPathArguments()) {
            if (item instanceof IdentifiableItem) {
                return createWildcard(path);
            }
        }
        return path;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <P extends InstanceIdentifier<?>> @NonNull P createWildcard(final @NonNull P path) {
        final var it = path.getPathArguments().iterator();
        final var first = it.next();
        final var firstCase = first.getCaseType();
        final var builder = firstCase.isPresent()
            ? InstanceIdentifier.builder((Class) firstCase.orElseThrow(), (Class) first.getType())
                : InstanceIdentifier.builder((Class) first.getType());

        while (it.hasNext()) {
            final var next = it.next();
            final var nextCase = next.getCaseType();
            if (nextCase.isPresent()) {
                builder.child(nextCase.orElseThrow(), next.getType());
            } else {
                builder.child(next.getType());
            }
        }

        return (P) builder.build();
    }
}
