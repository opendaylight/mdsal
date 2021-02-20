/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

abstract class TypeReference {
    private static final class Identityref extends TypeReference {
        private final List<IdentityGenerator> referencedGenerators;

        Identityref(final List<IdentityGenerator> referencedGenerators) {
            this.referencedGenerators = requireNonNull(referencedGenerators);
        }
    }

    private static final class ResolvedLeafref extends TypeReference {
        private final AbstractTypeObjectGenerator<?> referencedGenerator;

        ResolvedLeafref(final AbstractTypeObjectGenerator<?> referencedGenerator) {
            this.referencedGenerator = requireNonNull(referencedGenerator);
        }
    }

    private static final class UnresolvedLeafref extends TypeReference {
        static final @NonNull UnresolvedLeafref INSTANCE = new UnresolvedLeafref();

        private UnresolvedLeafref() {
            // Hidden on purpose
        }
    }

    static @NonNull TypeReference leafRef(final @Nullable AbstractTypeObjectGenerator<?> referencedGenerator) {
        return referencedGenerator == null ? UnresolvedLeafref.INSTANCE : new ResolvedLeafref(referencedGenerator);
    }

    static @NonNull TypeReference identityRef(final List<IdentityGenerator> referencedGenerators) {
        return new Identityref(referencedGenerators);
    }
}
