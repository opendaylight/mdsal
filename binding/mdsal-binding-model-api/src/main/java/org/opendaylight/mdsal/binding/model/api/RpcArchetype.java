/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * An archetype for an interface generated for a particular {@code rpc} statement.
 */
@NonNullByDefault
public record RpcArchetype(
        JavaTypeName typeName,
        RpcEffectiveStatement statement,
        JavaTypeName inputName,
        JavaTypeName outputName) implements Archetype.WithStatement {
    public RpcArchetype {
        requireNonNull(typeName);
        requireNonNull(statement);
        requireNonNull(inputName);
        requireNonNull(outputName);
    }

    @Override
    public Class<JavaConstruct.Interface> construct() {
        return JavaConstruct.Interface.class;
    }
}