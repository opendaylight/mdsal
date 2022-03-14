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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.KeyRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

@Beta
public class DefaultListRuntimeType extends AbstractAugmentableRuntimeType<ListEffectiveStatement>
        implements ListRuntimeType {
    private final @Nullable KeyRuntimeType keyType;

    DefaultListRuntimeType(final GeneratedType bindingType, final ListEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments, final KeyRuntimeType keyType) {
        super(bindingType, statement, children, augments);
        this.keyType = keyType;
    }

    public static @NonNull ListRuntimeType of(final GeneratedType bindingType, final ListEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments, final KeyRuntimeType keyType) {
        return new DefaultListRuntimeType(bindingType, statement, children, augments, keyType);
    }

    public static @NonNull ListRuntimeType of(final GeneratedType bindingType, final ListEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments, final KeyRuntimeType keyType,
            final List<AugmentRuntimeType> referencingAugments) {
        return referencingAugments.isEmpty() ? of(bindingType, statement, children, augments, keyType)
            : new ReferencedListRuntimeType(bindingType, statement, children, augments, keyType, referencingAugments);
    }

    @Override
    public final KeyRuntimeType keyType() {
        return keyType;
    }
}
