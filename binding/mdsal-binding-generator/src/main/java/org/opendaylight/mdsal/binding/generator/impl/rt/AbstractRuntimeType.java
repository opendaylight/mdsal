/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class AbstractRuntimeType<S extends EffectiveStatement<?, ?>, T extends GeneratedType>
        implements RuntimeType<S, T> {
    private final @NonNull T bindingType;
    private final @NonNull S schema;

    AbstractRuntimeType(final T bindingType, final S schema) {
        this.bindingType = requireNonNull(bindingType);
        this.schema = requireNonNull(schema);
    }

    @Override
    public final T bindingType() {
        return bindingType;
    }

    @Override
    public final S schema() {
        return schema;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("bindingType", bindingType).add("schema", schema).toString();
    }
}
