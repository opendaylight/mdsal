/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * The archetype of a class generated for a particular {@code type} statement holding a single value.
 */
@NonNullByDefault
public final class ScalarArchetype extends TypeObjectArchetype {
    public ScalarArchetype(final JavaTypeName typeName, final TypeEffectiveStatement<?> statement) {
        super(typeName, statement);
    }

    public ScalarArchetype(final JavaTypeName typeName, final TypedefEffectiveStatement statement) {
        super(typeName, statement);
    }

    @Override
    public Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }
}