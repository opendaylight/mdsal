/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.Archetype.WithGroupings;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@link #statement()}.
 */
@NonNullByDefault
public abstract sealed class DataObjectArchetype<S extends SchemaTreeAwareEffectiveStatement<?, ?>>
        extends AbstractArchetype<S> implements WithGroupings
        permits AugmentationArchetype, InputArchetype, InstanceNotificationArchetype, KeyAwareArchetype,
                NotificationArchetype, OutputArchetype {
    private final ImmutableList<DataObjectField<?>> fields;
    private final ImmutableList<JavaTypeName> groupings;

    DataObjectArchetype(final JavaTypeName typeName, final S statement,
            final ImmutableList<DataObjectField<?>> fields, final ImmutableList<JavaTypeName> groupings) {
        super(typeName, statement);
        this.fields = requireNonNull(fields);
        this.groupings = requireNonNull(groupings);
    }

    @Override
    public final Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    // Implies getters with general shape:
    //   public field.type() 'get' + capitalize(field.name())()
    // FIXME: communicate nullness and defaults and similar
    public final ImmutableList<DataObjectField<?>> fields() {
        return fields;
    }

    @Override
    public final ImmutableList<JavaTypeName> groupings() {
        return groupings;
    }

    @Override
    final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);
        if (!fields.isEmpty()) {
            helper.add("fields", fields);
        }
        if (!fields.isEmpty()) {
            helper.add("groupings", groupings);
        }
        return helper;
    }
}