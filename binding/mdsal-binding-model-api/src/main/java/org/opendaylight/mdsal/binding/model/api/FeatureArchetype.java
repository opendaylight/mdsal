/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * The archetype of a class generated for a particular {@link FeatureEffectiveStatement}.
 */
@NonNullByDefault
public record FeatureArchetype(JavaTypeName typeName, FeatureEffectiveStatement statement, JavaTypeName moduleDataName)
        implements Archetype.WithStatement, Archetype.WithClass {
    public FeatureArchetype {
        requireNonNull(typeName);
        requireNonNull(statement);
        requireNonNull(moduleDataName);
    }

    @Override
    public @Nullable Type superClass() {
        return null;
    }
}