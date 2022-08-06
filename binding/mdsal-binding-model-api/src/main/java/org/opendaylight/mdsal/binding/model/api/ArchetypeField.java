/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
public sealed interface ArchetypeField extends Immutable permits DataObjectField, BitsField, UnionField {
    /**
     * Return associated {@link EffectiveStatement}.
     *
     * @return associated {@link EffectiveStatement}
     */
    EffectiveStatement<?, ?> statement();

    String name();

    Type type();

    default ValueMechanics mechanics() {
        return ValueMechanics.NORMAL;
    }
}