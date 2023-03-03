/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

public final class DefaultGroupingRuntimeType extends AbstractCompositeRuntimeType<GroupingEffectiveStatement>
        implements GroupingRuntimeType {
    private final @Nullable Object instantiationVectors;

    public DefaultGroupingRuntimeType(final GeneratedType bindingType, final GroupingEffectiveStatement statement,
            final List<RuntimeType> children, final List<CompositeRuntimeType> instantiationVectors) {
        super(bindingType, statement, children);
        this.instantiationVectors = switch (instantiationVectors.size()) {
            case 0 -> null;
            case 1 -> Objects.requireNonNull(instantiationVectors.get(0));
            default -> instantiationVectors.stream().map(Objects::requireNonNull).toArray(CompositeRuntimeType[]::new);
        };
    }

    @Override
    public List<CompositeRuntimeType> instantiationVectors() {
        final var local = instantiationVectors;
        if (local == null) {
            return List.of();
        }
        return local instanceof CompositeRuntimeType composite ? List.of(composite)
            : Collections.unmodifiableList(Arrays.asList((CompositeRuntimeType[]) local));
    }
}
