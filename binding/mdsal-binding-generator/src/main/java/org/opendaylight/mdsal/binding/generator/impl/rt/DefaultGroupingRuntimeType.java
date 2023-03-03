/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.base.VerifyException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

public final class DefaultGroupingRuntimeType extends AbstractCompositeRuntimeType<GroupingEffectiveStatement>
        implements GroupingRuntimeType {
    /**
     * These are vectors towards concrete instantiations of this type -- i.e. the manifestation in the effective data
     * tree. Each item in this list represents either:
     * <ul>
     *   <li>a concrete instantiation, or<li>
     *   <li>another {@link GroupingRuntimeType}</li>
     * </ul>
     * We use these vectors to create {@link #instantiations()}.
     */
    private final @Nullable Object instantiationVectors;

    public DefaultGroupingRuntimeType(final GeneratedType bindingType, final GroupingEffectiveStatement statement,
            final List<RuntimeType> children, final List<? extends CompositeRuntimeType> instantiationVectors) {
        super(bindingType, statement, children);
        this.instantiationVectors = switch (instantiationVectors.size()) {
            case 0 -> null;
            case 1 -> Objects.requireNonNull(instantiationVectors.get(0));
            default -> instantiationVectors.stream().map(Objects::requireNonNull).toArray(CompositeRuntimeType[]::new);
        };
    }

    @Override
    public Set<CompositeRuntimeType> instantiations() {
        final var local = instantiationVectors;
        if (local == null) {
            return Set.of();
        }

        final var ret = new HashSet<CompositeRuntimeType>();
        resolveVectors(ret, local);
        return ret;
    }

    private void resolveVectors(final Set<CompositeRuntimeType> set) {
        final var local = instantiationVectors;
        if (local != null) {
            resolveVectors(set, local);
        }
    }

    private static void resolveVectors(final Set<CompositeRuntimeType> set, final @NonNull Object vector) {
        if (vector instanceof CompositeRuntimeType composite) {
            resolveVector(set, composite);
        } else if (vector instanceof CompositeRuntimeType[] composites) {
            for (var composite : composites) {
                resolveVector(set, composite);
            }
        } else {
            throw new VerifyException("Unhandled vectors " + vector.getClass());
        }
    }

    private static void resolveVector(final Set<CompositeRuntimeType> set, final CompositeRuntimeType vector) {
        if (vector instanceof DefaultGroupingRuntimeType thisImpl) {
            thisImpl.resolveVectors(set);
        } else if (vector instanceof GroupingRuntimeType grouping) {
            set.addAll(grouping.instantiations());
        } else {
            set.add(vector);
        }
    }
}
