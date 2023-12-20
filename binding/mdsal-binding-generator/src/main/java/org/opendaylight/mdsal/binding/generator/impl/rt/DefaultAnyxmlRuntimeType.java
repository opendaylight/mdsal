/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.OpaqueObjectArchetype;
import org.opendaylight.mdsal.binding.runtime.api.AnyxmlRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;

public final class DefaultAnyxmlRuntimeType
        extends AbstractGeneratedRuntimeType<AnyxmlEffectiveStatement, OpaqueObjectArchetype>
        implements AnyxmlRuntimeType {
    public DefaultAnyxmlRuntimeType(final OpaqueObjectArchetype archetype, final AnyxmlEffectiveStatement statement) {
        super(archetype, statement);
    }

    @Override
    public AnyxmlEffectiveStatement statement() {
        // TODO Auto-generated method stub
        return (AnyxmlEffectiveStatement) javaType().statement();
    }
}
