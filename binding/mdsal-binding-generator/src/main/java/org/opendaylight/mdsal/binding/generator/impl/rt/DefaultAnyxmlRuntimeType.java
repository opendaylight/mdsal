/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.AnyxmlArchetype;
import org.opendaylight.mdsal.binding.runtime.api.AnyxmlRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;

@NonNullByDefault
public final class DefaultAnyxmlRuntimeType
        extends AbstractGeneratedRuntimeType<AnyxmlEffectiveStatement, AnyxmlArchetype>
        implements AnyxmlRuntimeType {
    public DefaultAnyxmlRuntimeType(final AnyxmlArchetype archetype) {
        super(archetype);
    }
}
