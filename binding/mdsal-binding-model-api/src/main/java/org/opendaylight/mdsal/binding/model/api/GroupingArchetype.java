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
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

@NonNullByDefault
public record GroupingArchetype(
        JavaTypeName typeName,
        GroupingEffectiveStatement statement,
        ImmutableList<DataObjectField<?>> fields) implements Archetype.WithStatement, Archetype.WithInterface {
    public GroupingArchetype {
        requireNonNull(typeName);
        requireNonNull(statement);
        requireNonNull(fields);
    }
}