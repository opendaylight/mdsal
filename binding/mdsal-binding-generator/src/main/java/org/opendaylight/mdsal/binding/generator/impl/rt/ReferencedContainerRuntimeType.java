/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ReferencedAugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

final class ReferencedContainerRuntimeType extends DefaultContainerRuntimeType
        implements ReferencedAugmentableRuntimeType {
    private final @NonNull List<AugmentRuntimeType> referencingAugments;

    ReferencedContainerRuntimeType(final GeneratedType bindingType, final ContainerEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments,
            final List<AugmentRuntimeType> referencingAugments) {
        super(bindingType, statement, children, augments);
        this.referencingAugments = ImmutableList.copyOf(referencingAugments);
    }

    @Override
    public List<AugmentRuntimeType> referencingAugments() {
        return referencingAugments;
    }
}
