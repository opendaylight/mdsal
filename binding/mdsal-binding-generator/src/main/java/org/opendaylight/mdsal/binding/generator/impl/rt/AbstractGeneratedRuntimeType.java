/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.Archetype;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
abstract class AbstractGeneratedRuntimeType<S extends EffectiveStatement<?, ?>, A extends Archetype<S>>
        extends AbstractRuntimeType<S, A> {
    AbstractGeneratedRuntimeType(final A archetype) {
        super(archetype);
    }

    @Override
    public final S statement() {
        return javaType().statement();
    }
}
