/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
public abstract sealed class AbstractArchetype<S extends EffectiveStatement<?, ?>> implements Archetype<S>
        permits TypeObjectArchetype {
    private final JavaTypeName typeName;
    private final S statement;

    AbstractArchetype(final JavaTypeName typeName, final S statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final S statement() {
        return statement;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("typeName", typeName).add("statement", statement);
    }
}
