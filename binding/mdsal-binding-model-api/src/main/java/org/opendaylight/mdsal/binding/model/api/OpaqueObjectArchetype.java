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
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * An archetype for an interface generated for a particular {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
public final class OpaqueObjectArchetype implements Archetype.WithStatement {
    private final JavaTypeName typeName;
    private final DataTreeEffectiveStatement<?> statement;

    private OpaqueObjectArchetype(final JavaTypeName typeName, final DataTreeEffectiveStatement<?> statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
    }

    public OpaqueObjectArchetype(final JavaTypeName typeName, final AnydataEffectiveStatement statement) {
        this(typeName, (DataTreeEffectiveStatement<?>) statement);
    }

    public OpaqueObjectArchetype(final JavaTypeName typeName, final AnyxmlEffectiveStatement statement) {
        this(typeName, (DataTreeEffectiveStatement<?>) statement);
    }

    @Override
    public Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    @Override
    public DataTreeEffectiveStatement<?> statement() {
        return statement;
    }

    @Override
    public JavaTypeName typeName() {
        return typeName;
    }
}