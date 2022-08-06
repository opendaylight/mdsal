/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.errorprone.annotations.DoNotMock;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface KeyArchetype extends Archetype.WithStatement {
    @Override
    // FIXME: JavaConstruct.Record
    default Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    @Override
    KeyEffectiveStatement statement();

    JavaTypeName keyAwareName();

    List<DataObjectField<?>> fields();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends KeyArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}