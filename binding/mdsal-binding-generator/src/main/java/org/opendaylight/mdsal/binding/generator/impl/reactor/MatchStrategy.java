/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;


/**
 * A strategy for matching an {@link EffectiveStatement} to an {@link AbstractExplicitGenerator}.
 */
abstract class MatchStrategy {
    /**
     * Strategy matching on exact statement identity. We use this initially as it works for non-weird cases.
     */
    private static final class Identity extends MatchStrategy {
        static final @NonNull Identity INSTANCE = new Identity();

        @Override
        AbstractExplicitGenerator<?> findGenerator(final EffectiveStatement<?, ?> needle,
                final Iterable<? extends Generator> haystack) {
            for (Generator gen : haystack) {
                if (gen instanceof AbstractExplicitGenerator) {
                    final AbstractExplicitGenerator<?> ret = (AbstractExplicitGenerator<?>) gen;
                    if (needle == ret.statement()) {
                        return ret;
                    }
                }
            }

            return null;
        }
    }

    /**
     * Strategy matching on exact QName argument. Used when we are switching along the 'augments' axis.
     */
    private static class OnQName extends MatchStrategy {
        static final @NonNull OnQName INSTANCE = new OnQName();

        @Override
        final AbstractExplicitGenerator<?> findGenerator(final EffectiveStatement<?, ?> needle,
                final Iterable<? extends Generator> haystack) {
            final Object needleArg = needle.argument();
            verify(needleArg instanceof QName, "Unexpected argument %s in %s", needleArg, needle);

            return findGenerator((QName) needleArg, haystack);
        }

        AbstractExplicitGenerator<?> findGenerator(final QName needle, final Iterable<? extends Generator> haystack) {
            for (Generator gen : haystack) {
                if (gen instanceof AbstractExplicitGenerator) {
                    final AbstractExplicitGenerator<?> ret = (AbstractExplicitGenerator<?>) gen;
                    if (needle.equals(ret.statement().argument())) {
                        return ret;
                    }
                }
            }
            return null;
        }
    }

    private static final class Grouping extends OnQName {
        private final @NonNull QNameModule groupingModule;

        Grouping(final GroupingGenerator grouping) {
            groupingModule = grouping.statement().argument().getModule();
        }

        @Override
        AbstractExplicitGenerator<?> findGenerator(final QName needle, final Iterable<? extends Generator> haystack) {
            return super.findGenerator(needle.bindTo(groupingModule), haystack);
        }
    }

    private MatchStrategy() {
        // Hidden on purpose
    }

    static @NonNull MatchStrategy augment() {
        return OnQName.INSTANCE;
    }

    static @NonNull MatchStrategy grouping(final GroupingGenerator grouping) {
        return new Grouping(grouping);
    }

    static @NonNull MatchStrategy identity() {
        return Identity.INSTANCE;
    }

    abstract @Nullable AbstractExplicitGenerator<?> findGenerator(EffectiveStatement<?, ?> needle,
            Iterable<? extends Generator> haystack);
}
