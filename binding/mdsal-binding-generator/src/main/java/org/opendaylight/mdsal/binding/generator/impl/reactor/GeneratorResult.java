/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Archetype;
import org.opendaylight.yangtools.concepts.Immutable;

sealed class GeneratorResult implements Immutable {
    private static final class Nested extends GeneratorResult {
        Nested(final Archetype<?> archetype) {
            super(archetype);
        }

        @Override
        Archetype<?> enclosedType() {
            return archetype();
        }
    }

    private static final @NonNull GeneratorResult EMPTY = new GeneratorResult();

    private final @Nullable Archetype<?> archetype;

    private GeneratorResult() {
        archetype = null;
    }

    private GeneratorResult(final Archetype<?> archetype) {
        this.archetype = requireNonNull(archetype);
    }

    static @NonNull GeneratorResult empty() {
        return EMPTY;
    }

    static @NonNull GeneratorResult member(final Archetype<?> archetype) {
        return new Nested(archetype);
    }

    static @NonNull GeneratorResult toplevel(final Archetype<?> archetype) {
        return new GeneratorResult(archetype);
    }

    final @Nullable Archetype<?> archetype() {
        return archetype;
    }

    @Nullable Archetype<?> enclosedType() {
        return null;
    }
}
