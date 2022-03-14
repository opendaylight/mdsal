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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@Beta
public class DefaultContainerRuntimeType extends AbstractAugmentableRuntimeType<ContainerEffectiveStatement>
        implements ContainerRuntimeType {
    DefaultContainerRuntimeType(final GeneratedType bindingType, final ContainerEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
        super(bindingType, statement, children, augments);
    }

    public static @NonNull ContainerRuntimeType of(final GeneratedType bindingType,
            final ContainerEffectiveStatement statement, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments) {
        return new DefaultContainerRuntimeType(bindingType, statement, children, augments);
    }

    public static @NonNull ContainerRuntimeType of(final GeneratedType bindingType,
            final ContainerEffectiveStatement statement, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments, final List<AugmentRuntimeType> referencingAugments) {
        return referencingAugments.isEmpty() ? of(bindingType, statement, children, augments)
            : new ReferencedContainerRuntimeType(bindingType, statement, children, augments, referencingAugments);
    }
}
