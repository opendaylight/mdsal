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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * The archetype of a class generated for a particular {@code type bits} statement statement.
 */
@NonNullByDefault
public final class BitsArchetype extends TypeObjectArchetype {
    private final ImmutableList<BitsField> fields;

    public BitsArchetype(final JavaTypeName typeName, final TypeEffectiveStatement<?> statement,
            final ImmutableList<BitsField> fields) {
        super(typeName, statement);
        this.fields = requireNonNull(fields);
    }

    public BitsArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement,
            final ImmutableList<BitsField> fields) {
        super(typeName, statement);
        this.fields = requireNonNull(fields);
    }

    @Override
    public Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    // corresponds to
    //   statement().streamEffectiveSubstatements(BitEffectiveStatement.class).map( create Field )
    public ImmutableList<BitsField> fields() {
        return fields;
    }
}