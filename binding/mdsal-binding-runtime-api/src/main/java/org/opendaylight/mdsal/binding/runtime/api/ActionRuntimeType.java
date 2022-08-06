/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.opendaylight.mdsal.binding.model.api.Archetype;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * A {@link RuntimeType} associated with an {@code action} statement.
 */
public interface ActionRuntimeType extends InvokableRuntimeType {
    @Override
    ActionEffectiveStatement statement();

    @Override
    Archetype.Action archetype();
}
