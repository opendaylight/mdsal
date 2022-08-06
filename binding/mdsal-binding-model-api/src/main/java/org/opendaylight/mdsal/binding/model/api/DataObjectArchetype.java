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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@link #statement()}.
 */
@NonNullByDefault
public abstract sealed class DataObjectArchetype<S extends SchemaTreeAwareEffectiveStatement<?, ?>>
        implements Archetype.WithStatement
        permits AugmentationArchetype, InputArchetype, InstanceNotificationArchetype, KeyAwareArchetype,
                NotificationArchetype, OutputArchetype {
    private final JavaTypeName typeName;
    private final S statement;
    private final ImmutableList<DataObjectField<?>> fields;

    DataObjectArchetype(final JavaTypeName typeName, final S statement,
            final ImmutableList<DataObjectField<?>> fields) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
        this.fields = requireNonNull(fields);
    }

    @Override
    public final Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final S statement() {
        return statement;
    }

    public final ImmutableList<DataObjectField<?>> fields() {
        return fields;
    }
}