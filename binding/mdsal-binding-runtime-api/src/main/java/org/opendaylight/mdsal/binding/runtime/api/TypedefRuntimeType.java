/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.opendaylight.mdsal.binding.model.api.Archetype;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code typedef} statement.
 */
public interface TypedefRuntimeType extends GeneratedRuntimeType {
    @Override
    TypedefEffectiveStatement statement();

    @Override
    GeneratedType javaType();

    @Override
    Archetype.TypeObject archetype();
}
