/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.DoNotMock;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;

/**
 * The archetype of an enum generated for a particular {@code type enumeration} statement.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface EnumerationArchetype extends TypeObjectArchetype {
    @Override
    default Class<JavaConstruct.Enum> construct() {
        return JavaConstruct.Enum.class;
    }

    // corresponds to
    //   statement().streamEffectiveSubstatements(EnumEffectiveStatement.class).map( create Constant )
    List<EnumerationArchetype.Constant> constants();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends EnumerationArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }

    // FIXME: can we pick 'value' from stmt?
    record Constant(EnumEffectiveStatement stmt, String javaName, int value) {
        public Constant {
            requireNonNull(stmt);
            // Note: corresponds to JavaTypeName.simpleName()
            requireNonNull(javaName);
        }
    }
}