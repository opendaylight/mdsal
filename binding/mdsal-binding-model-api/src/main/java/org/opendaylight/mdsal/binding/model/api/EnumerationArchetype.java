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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * The archetype of an enum generated for a particular {@code type enumeration} statement.
 */
@NonNullByDefault
public final class EnumerationArchetype extends TypeObjectArchetype {
    private final ImmutableList<EnumerationConstant> constants;

    public EnumerationArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement,
            final ImmutableList<EnumerationConstant> constants) {
        super(typeName, statement);
        this.constants = requireNonNull(constants);
    }

    // corresponds to
    //   statement().streamEffectiveSubstatements(EnumEffectiveStatement.class).map( create Constant )
    public ImmutableList<EnumerationConstant> constants() {
        return constants;
    }
}