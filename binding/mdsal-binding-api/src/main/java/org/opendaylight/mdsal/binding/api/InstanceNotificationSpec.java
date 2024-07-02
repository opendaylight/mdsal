/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A combination of an {@link InstanceNotification} class and its corresponding instantiation wildcard, expressed as
 * an {@link DataObjectReference}. This glue is required because instance notification interfaces are generated at the
 * place of their definition, most importantly in {@code grouping} and we actually need to bind to a particular
 * instantiation (e.g. a place where {@code uses} references that grouping).
 *
 * @param <N> Generated InstanceNotification interface type
 * @param <P> Instance notification parent type
 */
@Beta
public final class InstanceNotificationSpec<N extends InstanceNotification<N, P>, P extends DataObject>
        implements Immutable {
    private final @NonNull DataObjectReference<P> path;
    private final @NonNull Class<N> type;

    private InstanceNotificationSpec(final @NonNull Class<N> type, final @NonNull InstanceIdentifier<P> path) {
        this.type = requireNonNull(type);
        this.path = path.toReference();
    }

    public static <P extends ChildOf<? extends DataRoot<?>>> @NonNull Builder<P> builder(
            final @NonNull Class<P> container) {
        return new Builder<>(InstanceIdentifier.builder(container));
    }

    public static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject, P extends ChildOf<? super C>>
            @NonNull Builder<P> builder(final @NonNull Class<C> caze, final @NonNull Class<P> container) {
        return new Builder<>(InstanceIdentifier.builder(caze, container));
    }

    public @NonNull DataObjectReference<P> path() {
        return path;
    }

    public @NonNull Class<N> type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof final InstanceNotificationSpec<?, ?> other
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

        public <N extends ChildOf<? super P>> @NonNull Builder<N> withPathChild(final @NonNull Class<N> container) {
            pathBuilder.child(container);
            return castThis();
        }

        public <C extends ChoiceIn<? super P> & DataObject, N extends ChildOf<? super C>>
                @NonNull Builder<N> withPathChild(final @NonNull Class<C> caze, final @NonNull Class<N> container) {
            pathBuilder.child(caze, container);
            return castThis();
        }

        public <A extends Augmentation<? super P>> @NonNull Builder<A> withPathAugmentation(
                final @NonNull Class<A> augmentation) {
            pathBuilder.augmentation(augmentation);
            return castThis();
        }

        public <N extends InstanceNotification<N, P>> @NonNull InstanceNotificationSpec<N, P> build(
                final @NonNull Class<N> type) {
            return new InstanceNotificationSpec<>(type, pathBuilder.build());
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject> @NonNull Builder<N> castThis() {
            return (Builder<N>) this;
        }
    }
}
