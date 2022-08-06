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
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

@NonNullByDefault
public record DataObjectField<S extends DataTreeEffectiveStatement<?>>(S statement, String name, Type type)
        implements ArchetypeField {
    public DataObjectField {
        requireNonNull(statement);
        requireNonNull(name);
        requireNonNull(type);
    }
}