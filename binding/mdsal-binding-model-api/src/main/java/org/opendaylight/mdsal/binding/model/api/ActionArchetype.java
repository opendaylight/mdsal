/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.errorprone.annotations.DoNotMock;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * The archetype of an interface generated for an {@code action}.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface ActionArchetype extends Archetype.WithStatement {
    @Override
    default Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    @Override
    ActionEffectiveStatement statement();

    JavaTypeName inputName();

    JavaTypeName outputName();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends ActionArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}