/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

@NonNullByDefault
public abstract sealed class TypeObjectArchetype extends AbstractArchetype<EffectiveStatement<?, ?>>
        permits BitsArchetype, EnumerationArchetype, ScalarArchetype, UnionArchetype {
    TypeObjectArchetype(final JavaTypeName typeName, final TypeEffectiveStatement<?> statement) {
        super(typeName, statement);
    }

    TypeObjectArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement) {
        super(typeName, statement);
    }

    public final TypeEffectiveStatement<?> effectiveType() {
        final var statement = statement();
        return statement instanceof TypeEffectiveStatement<?> typeStmt ? typeStmt
            : ((TypedefEffectiveStatement) statement).asTypeEffectiveStatement();
    }
}