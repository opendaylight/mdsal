/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;


/**
 * Transitional interface to for expressing proper {@link GeneratedType} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
@NonNullByDefault
public sealed interface Archetype extends Type permits Archetype.WithStatement, BuilderArchetype {

    JavaTypeName typeName();

    @Override
    @Deprecated(forRemoval = true)
    default JavaTypeName getIdentifier() {
        return typeName();
    }

    /**
     * Return the underlying {@link JavaConstruct} type.
     *
     * @return the underlying {@link JavaConstruct} type
     */
    // FIXME: this really looks like a java-api-generator concern and we should not carry it here
    Class<? extends JavaConstruct> construct();

    /**
     * An {@link Archetype} which has an associated {@link EffectiveStatement}.
     */
    sealed interface WithStatement extends Archetype
            permits ActionArchetype, AnnotationArchetype, ChoiceArchetype, DataObjectArchetype, FeatureArchetype,
                    GroupingArchetype, IdentityArchetype, KeyArchetype, OpaqueObjectArchetype, RpcArchetype,
                    TypeObjectArchetype {
        /**
         * Return associated {@link EffectiveStatement}.
         *
         * @return associated {@link EffectiveStatement}
         */
        EffectiveStatement<?, ?> statement();
    }
}
