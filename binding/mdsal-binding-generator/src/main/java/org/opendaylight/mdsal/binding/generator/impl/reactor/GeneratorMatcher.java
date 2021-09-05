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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class GeneratorMatcher {
    static final @NonNull GeneratorMatcher AUGMENT = new GeneratorMatcher() {
        @Override
        AbstractExplicitGenerator<?> findGenerator(final EffectiveStatement<?, ?> needle,
                final Iterable<? extends Generator> haystack) {
            final Object needleArg = needle.argument();
            verify(needleArg instanceof QName, "Unexpected argument %s in %s", needleArg, needle);
            final QName qname = (QName) needleArg;

            for (Generator gen : haystack) {
                if (gen instanceof AbstractExplicitGenerator) {
                    final AbstractExplicitGenerator<?> ret = (AbstractExplicitGenerator<?>) gen;
                    if (qname.equals(ret.statement().argument())) {
                        return ret;
                    }
                }
            }
            return null;
        }
    };
    static final @NonNull GeneratorMatcher IDENTITY = new GeneratorMatcher() {
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
    };

    private GeneratorMatcher() {
        // Hidden on purpose
    }

    abstract @Nullable AbstractExplicitGenerator<?> findGenerator(EffectiveStatement<?, ?> needle,
            Iterable<? extends Generator> haystack);
}
