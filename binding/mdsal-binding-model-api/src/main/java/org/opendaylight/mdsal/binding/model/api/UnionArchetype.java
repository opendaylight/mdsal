/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.errorprone.annotations.DoNotMock;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;

/**
 * The archetype of a class generated for a particular {@code type union} statement.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface UnionArchetype extends TypeObjectArchetype {
    @Override
    default Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    // corresponds to statement().collectEffectiveSubstatements(TypeEffectiveStatement.class)
    List<UnionField> fields();

    // corresponds to
    //  statement().streamEffectiveSubstatements(TypeEffectiveStatement.class)
    //    .filter( needs to have a nested classes generated )
    default List<TypeObjectArchetype> generatedTypes() {
        return fields().stream()
            .map(UnionField::type)
            .filter(TypeObjectArchetype.class::isInstance).map(TypeObjectArchetype.class::cast)
            .collect(Collectors.toList());
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends UnionArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}