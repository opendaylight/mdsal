/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@link #statement()}.
 */
@NonNullByDefault
public sealed interface DataObjectArchetype<S extends SchemaTreeAwareEffectiveStatement<?, ?>>
        extends Archetype.WithGroupings<S>
        permits AugmentationArchetype, InputArchetype, InstanceNotificationArchetype, ListArchetype,
                NotificationArchetype, OutputArchetype {

    // Implies getters with general shape:
    //   public field.type() 'get' + capitalize(field.name())()
    // FIXME: communicate nullness and defaults and similar
    ImmutableList<DataObjectField<?>> fields();
}