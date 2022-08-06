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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

@NonNullByDefault
public abstract sealed class TypeObjectArchetype implements Archetype.WithStatement
        permits BitsArchetype, EnumerationArchetype, ScalarArchetype, UnionArchetype {
    private final JavaTypeName typeName;
    private final EffectiveStatement<?, ?> statement;

    private TypeObjectArchetype(final JavaTypeName typeName, final EffectiveStatement<?, ?> statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
    }

    TypeObjectArchetype(final JavaTypeName typeName, final TypeEffectiveStatement<?> statement) {
        this(typeName, (EffectiveStatement<?, ?>) statement);
    }

    TypeObjectArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement) {
        this(typeName, (EffectiveStatement<?, ?>) statement);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final EffectiveStatement<?, ?> statement() {
        return statement;
    }

    public final TypeEffectiveStatement<?> effectiveType() {
        return statement instanceof TypeEffectiveStatement<?> typeStmt ? typeStmt
            : ((TypedefEffectiveStatement) statement).asTypeEffectiveStatement();
    }
}