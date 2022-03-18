/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;

/**
 * Utility to resolve instantiated {@code augment} statements to their {@link AbstractAugmentGenerator} counterparts.
 * This is essentially a stack of {@link AbstractCompositeGenerator}s which should be examined.
 */
final class AugmentResolver implements Mutable {
    private final Deque<AbstractCompositeGenerator<?, ?>> stack = new ArrayDeque<>();

    void enter(final AbstractCompositeGenerator<?, ?> generator) {
        stack.push(requireNonNull(generator));
    }

    void exit() {
        stack.pop();
    }

    @NonNull AbstractAugmentGenerator getAugment(final AugmentEffectiveStatement statement) {
        final var declared = verifyNotNull(statement.getDeclared(), " %s does not have a declared view", statement);
        for (var generator : stack) {
            final var found = findAugment(generator, declared);
            if (found != null) {
                return found;
            }
        }
        throw new IllegalStateException("Failed to resolve " + statement + " in " + stack);
    }

    private @Nullable AbstractAugmentGenerator findAugment(final AbstractCompositeGenerator<?, ?> generator,
            final AugmentStatement statement) {
        for (var augment : generator.augments()) {
            if (statement.equals(augment.statement().getDeclared())) {
                return augment;
            }
        }
        for (var grouping : generator.groupings()) {
            final var found = findAugment(grouping, statement);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
