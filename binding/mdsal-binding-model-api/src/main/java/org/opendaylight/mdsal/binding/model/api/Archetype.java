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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;


/**
 * Transitional interface to for expressing proper {@link Type} archetype.
 */
// FIXME: Remove this interface when we eliminate GeneratedTransferObject and direct GeneratedType builders
@Beta
@NonNullByDefault
public sealed interface Archetype<S extends EffectiveStatement<?, ?>> extends Type, Immutable
        permits Archetype.WithGroupings, ActionArchetype, AnnotationArchetype, ChoiceArchetype,
                DataObjectBuilderArchetype, FeatureArchetype, GroupingArchetype, IdentityArchetype, KeyArchetype,
                OpaqueObjectArchetype, RpcArchetype, AbstractArchetype {

    JavaTypeName typeName();

    /**
     * Return associated {@link EffectiveStatement}.
     *
     * @return associated {@link EffectiveStatement}
     */
    S statement();

    @Override
    @Deprecated(forRemoval = true)
    default JavaTypeName getIdentifier() {
        return typeName();
    }

    sealed interface WithGroupings<S extends EffectiveStatement<?, ?>> extends Archetype<S>
            permits DataObjectArchetype, YangDataArchetype {
        // Implies 'extends JavaTypeName'
        ImmutableList<JavaTypeName> groupings();
    }
}
