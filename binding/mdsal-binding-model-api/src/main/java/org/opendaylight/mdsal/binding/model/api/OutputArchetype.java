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
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@code output} statement.
 */
@NonNullByDefault
public record OutputArchetype(
        JavaTypeName typeName,
        OutputEffectiveStatement statement,
        ImmutableList<DataObjectField<?>> fields,
        ImmutableList<JavaTypeName> groupings) implements DataObjectArchetype<OutputEffectiveStatement> {
    public OutputArchetype {
        requireNonNull(typeName);
        requireNonNull(statement);
        requireNonNull(fields);
        requireNonNull(groupings);
    }
}