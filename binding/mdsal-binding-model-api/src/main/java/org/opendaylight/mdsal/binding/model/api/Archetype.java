/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Archetype.WithGroupings;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;


/**
 * Transitional interface to for expressing proper {@link GeneratedType} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
@NonNullByDefault
public sealed interface Archetype extends Type, Immutable
        permits Archetype.WithStatement, Archetype.WithClass, WithGroupings.WithGroupings, DataObjectBuilderArchetype {

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
            permits ActionArchetype, AnnotationArchetype, ChoiceArchetype, AbstractArchetype, FeatureArchetype,
                    GroupingArchetype, IdentityArchetype, KeyArchetype, RpcArchetype, YangDataArchetype {
        /**
         * Return associated {@link EffectiveStatement}.
         *
         * @return associated {@link EffectiveStatement}
         */
        EffectiveStatement<?, ?> statement();
    }

    sealed interface WithClass extends Archetype permits BitsArchetype, ScalarArchetype, UnionArchetype {
        @Override
        default Class<JavaConstruct.Class> construct() {
            return JavaConstruct.Class.class;
        }

        @Nullable Type superClass();
    }

    sealed interface WithGroupings extends Archetype permits DataObjectArchetype, YangDataArchetype {
        // Implies 'extends JavaTypeName'
        ImmutableList<JavaTypeName> groupings();
    }
}
