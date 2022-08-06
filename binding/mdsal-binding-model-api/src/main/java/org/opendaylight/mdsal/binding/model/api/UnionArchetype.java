/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Archetype.WithClass;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * The archetype of a class generated for a particular {@code type union} statement.
 */
@NonNullByDefault
public final class UnionArchetype extends TypeObjectArchetype implements WithClass {
    private final ImmutableList<UnionField> fields;

    UnionArchetype(final JavaTypeName typeName, final TypeEffectiveStatement<?> statement,
            final ImmutableList<UnionField> fields) {
        super(typeName, statement);
        this.fields = requireNonNull(fields);
    }

    UnionArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement,
            final ImmutableList<UnionField> fields) {
        super(typeName, statement);
        this.fields = requireNonNull(fields);
    }

    // corresponds to statement().collectEffectiveSubstatements(TypeEffectiveStatement.class)
    public ImmutableList<UnionField> fields() {
        return fields;
    }

    @Override
    public @Nullable Type superClass() {
        return null;
    }

    // corresponds to
    //  statement().streamEffectiveSubstatements(TypeEffectiveStatement.class)
    //    .filter( needs to have a nested classes generated )
    public List<TypeObjectArchetype> generatedTypes() {
        return fields().stream()
            .map(UnionField::type)
            .filter(TypeObjectArchetype.class::isInstance).map(TypeObjectArchetype.class::cast)
            .collect(Collectors.toList());
    }
}