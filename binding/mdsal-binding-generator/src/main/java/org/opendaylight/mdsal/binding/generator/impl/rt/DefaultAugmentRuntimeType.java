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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;

@Beta
public final class DefaultAugmentRuntimeType extends AbstractCompositeRuntimeType<AugmentEffectiveStatement>
        implements AugmentRuntimeType {
    private final @NonNull AugmentEffectiveStatement effectiveStatement;

    public DefaultAugmentRuntimeType(final GeneratedType bindingType, final AugmentEffectiveStatement statement,
            final Map<RuntimeType, EffectiveStatement<?, ?>> children, final List<AugmentRuntimeType> augments,
            final SchemaTreeAwareEffectiveStatement<?, ?> targetStatement) {
        super(bindingType, statement, children, augments);
        effectiveStatement = new TargetAugmentEffectiveStatement(statement, targetStatement);
    }

    @Override
    public AugmentEffectiveStatement effectiveStatement() {
        return effectiveStatement;
    }
}
