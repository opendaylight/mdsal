/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Link to the original definition of an {@link AbstractExplicitGenerator}.
 *
 */
// FIXME: sealed when we have JDK17+
abstract class OriginalLink implements Immutable {
    private static final class Complete extends OriginalLink {
        Complete(final AbstractExplicitGenerator<?> original) {
            super(original);
        }

        @Override
        AbstractExplicitGenerator<?> original() {
            return ref();
        }
    }

    private static final class Partial extends OriginalLink {
        Partial(final AbstractExplicitGenerator<?> ref) {
            super(ref);
        }

        @Override
        AbstractExplicitGenerator<?> original() {
            return null;
        }
    }

    private final AbstractExplicitGenerator<?> ref;

    private OriginalLink(final AbstractExplicitGenerator<?> ref) {
        this.ref = requireNonNull(ref);
    }

    static @NonNull OriginalLink complete(final AbstractExplicitGenerator<?> original) {
        return new Complete(original);
    }

    static @NonNull OriginalLink partial(final AbstractExplicitGenerator<?> source) {
        return new Partial(source);
    }

    final AbstractExplicitGenerator<?> ref() {
        return ref;
    }

    abstract @Nullable AbstractExplicitGenerator<?> original();

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", ref).toString();
    }
}
