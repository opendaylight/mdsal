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
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

@NonNullByDefault
public record KeyArchetype(
        JavaTypeName typeName,
        KeyEffectiveStatement statement,
        JavaTypeName keyAwareName,
        ImmutableList<DataObjectField<?>> fields) implements Archetype<KeyEffectiveStatement> {
    public KeyArchetype {
        requireNonNull(typeName);
        requireNonNull(keyAwareName);

        final var stmtFields = statement.argument();
        if (stmtFields.size() != fields.size()) {
            throw new IllegalArgumentException("Expected fields for " + stmtFields + ", got " + fields);
        }
        for (var field : fields) {
            final var qname = field.statement().argument();
            if (!stmtFields.contains(qname)) {
                throw new IllegalArgumentException("Field " + field + ", expecting all of " + stmtFields);
            }
        }
    }
}