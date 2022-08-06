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
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * An archetype for a {@code DataObject} specialization interface generated for a particular {@code augment} statement.
 */
@NonNullByDefault
public final class AugmentationArchetype extends DataObjectArchetype<AugmentEffectiveStatement> {
    public AugmentationArchetype(final JavaTypeName typeName, final AugmentEffectiveStatement statement,
            final ImmutableList<DataObjectField<?>> fields, final ImmutableList<JavaTypeName> groupings) {
        super(typeName, statement, fields, groupings);
    }
}