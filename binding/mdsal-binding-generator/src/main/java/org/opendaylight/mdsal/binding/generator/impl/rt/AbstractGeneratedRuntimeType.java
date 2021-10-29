/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class AbstractGeneratedRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> {
    AbstractGeneratedRuntimeType(final GeneratedType bindingType, final S statement) {
        super(bindingType, statement);
    }
}
