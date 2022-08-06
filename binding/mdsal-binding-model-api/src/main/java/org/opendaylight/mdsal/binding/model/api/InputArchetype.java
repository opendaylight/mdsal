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
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@code input} statement.
 */
@NonNullByDefault
public final class InputArchetype extends DataObjectArchetype<InputEffectiveStatement> {
    public InputArchetype(final JavaTypeName typeName, final InputEffectiveStatement statement,
            final ImmutableList<DataObjectField<?>> fields) {
        super(typeName, statement, fields);
    }
}