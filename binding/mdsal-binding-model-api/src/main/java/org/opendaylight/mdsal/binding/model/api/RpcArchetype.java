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
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * An archetype for an interface generated for a particular {@code rpc} statement.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface RpcArchetype extends Archetype.WithStatement {
    @Override
    default Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }

    @Override
    RpcEffectiveStatement statement();

    JavaTypeName inputName();

    JavaTypeName outputName();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends RpcArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}