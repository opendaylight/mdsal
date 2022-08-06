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
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;

/**
 * The archetype of a class generated for a particular {@code type yang-data} statement.
 */
@NonNullByDefault
public record YangDataArchetype(
        JavaTypeName typeName,
        YangDataEffectiveStatement statement,
        ImmutableList<DataObjectField<?>> fields,
        ImmutableList<JavaTypeName> groupings,
        JavaTypeName dataRoot) implements Archetype.WithStatement, Archetype.WithGroupings, Archetype.WithInterface {
    public YangDataArchetype {
        requireNonNull(typeName);
        requireNonNull(statement);
        requireNonNull(fields);
        requireNonNull(groupings);
        requireNonNull(dataRoot);
    }
}