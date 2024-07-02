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
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A combination of an {@link Action} class and its corresponding instantiation wildcard, expressed as
 * an {@link DataObjectReference}. This means that {@code list}s are treated exactly as @{code container}s are, e.g.
 * without a key value specification.
 *
 * <p>
 * This glue is required because action interfaces are generated at the place of their
 * definition, most importantly in {@code grouping} and we actually need to bind to a particular instantiation (e.g. a
 * place where {@code uses} references that grouping).
 *
 * @param <A> Generated Action interface type
 * @param <P> Action parent type
 */
@Beta
public final class ActionSpec<A extends Action<? extends DataObjectIdentifier<P>, ?, ?>, P extends DataObject>
        implements Immutable {
    private final @NonNull DataObjectReference<P> path;
    private final @NonNull Class<A> type;

    private ActionSpec(final @NonNull Class<A> type, final @NonNull InstanceIdentifier<P> path) {
        this.type = requireNonNull(type);
        this.path = path.toReference();
    }

    public static <P extends ChildOf<? extends DataRoot<?>>> @NonNull Builder<P> builder(
            @NonNull final Class<P> container) {
        return new Builder<>(InstanceIdentifier.builder(container));
    }

    public static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject, P extends ChildOf<? super C>>
            @NonNull Builder<P> builder(final @NonNull Class<C> caze, @NonNull final Class<P> container) {
        return new Builder<>(InstanceIdentifier.builder(caze, container));
    }

    public @NonNull DataObjectReference<P> path() {
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
        return obj == this || obj instanceof ActionSpec<?, ?> other
            && type.equals(other.type) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", type).add("path", path).toString();
    }

    @Beta
    public static final class Builder<P extends DataObject> implements Mutable {
        private final InstanceIdentifier.Builder<P> pathBuilder;

        Builder(final InstanceIdentifier.Builder<P> pathBuilder) {
            this.pathBuilder = requireNonNull(pathBuilder);
        }

        public <N extends ChildOf<? super P>> @NonNull Builder<N> withPathChild(@NonNull final Class<N> container) {
            pathBuilder.child(container);
            return castThis();
        }

        public <C extends ChoiceIn<? super P> & DataObject, N extends ChildOf<? super C>>
                @NonNull Builder<N> withPathChild(final Class<C> caze, @NonNull final Class<N> container) {
            pathBuilder.child(caze, container);
            return castThis();
        }

        public <A extends Augmentation<? super P>> @NonNull Builder<A> withPathAugmentation(
                @NonNull final Class<A> augmentation) {
            pathBuilder.augmentation(augmentation);
            return castThis();
        }

        public <A extends Action<? extends DataObjectIdentifier<P>, ?, ?>> @NonNull ActionSpec<A, P> build(
                @NonNull final Class<A> type) {
            return new ActionSpec<>(type, pathBuilder.build());
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject> @NonNull Builder<N> castThis() {
            return (Builder<N>) this;
        }
    }
}
