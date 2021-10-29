/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

@Beta
public final class DefaultListRuntimeType extends AbstractCompositeRuntimeType<ListEffectiveStatement>
        implements ListRuntimeType {
    public DefaultListRuntimeType(final GeneratedType bindingType, final ListEffectiveStatement statement,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children, final List<AugmentRuntimeType> augments) {
        super(bindingType, statement, children, augments);
    }

    @Override
    public KeyRuntimeType keyType() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
