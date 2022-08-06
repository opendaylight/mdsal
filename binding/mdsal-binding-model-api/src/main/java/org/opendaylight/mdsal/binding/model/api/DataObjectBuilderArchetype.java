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

/**
 * The archetype of a builder class generated for a particular {@link #target() target DataObject}.
 *
 * @param target target {@link DataObjectArchetype}
 */
@NonNullByDefault
public record DataObjectBuilderArchetype(JavaTypeName typeName, DataObjectArchetype<?> target) implements Archetype {
    public DataObjectBuilderArchetype {
        requireNonNull(typeName);
        requireNonNull(target);
    }

    @Override
    public Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }
}