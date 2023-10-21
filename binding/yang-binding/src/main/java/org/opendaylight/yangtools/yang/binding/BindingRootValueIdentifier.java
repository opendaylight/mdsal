/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.impl.BuiltBindingRootValueIdentifier;

/**
 * A {@link BindingInstanceIdentifier} identifying a value of a {@link DataRoot} property.
 */
public non-sealed interface BindingRootValueIdentifier<R extends DataRoot, V> extends BindingValueIdentifier<R, V> {

    @NonNull Class<R> rootType();

    final class Builder<R extends DataRoot, V> extends BindingValueIdentifier.Builder<R, V> {
        private final @NonNull Class<R> rootType;

        Builder(final Class<R> rootType, final Getter<R, V> getter) {
            super(getter);
            this.rootType = requireNonNull(rootType);
        }

        @Override
        public BindingRootValueIdentifier<R, V> build() {
            return new BuiltBindingRootValueIdentifier<>(rootType, getter);
        }
    }
}
