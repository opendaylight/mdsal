/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.InputArchetype;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

public final class DefaultInputRuntimeType
        extends AbstractAugmentableRuntimeType<InputEffectiveStatement, InputArchetype>
        implements InputRuntimeType {
    public DefaultInputRuntimeType(final InputArchetype archetype, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments) {
        super(archetype, children, augments);
    }
}
