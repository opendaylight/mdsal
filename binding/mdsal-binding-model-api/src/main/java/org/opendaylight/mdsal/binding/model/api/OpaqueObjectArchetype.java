/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * An archetype for an interface generated for a particular {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
public sealed interface OpaqueObjectArchetype<S extends DataTreeEffectiveStatement<?>> extends Archetype<S>
        permits AnydataArchetype, AnyxmlArchetype {

    JavaTypeName childOf();
}