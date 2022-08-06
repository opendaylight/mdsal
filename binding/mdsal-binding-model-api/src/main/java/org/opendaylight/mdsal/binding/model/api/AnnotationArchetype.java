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
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;

@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface AnnotationArchetype extends Archetype.WithStatement {
    @Override
    // FIXME: JavaConstruct.Record
    default Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    @Override
    AnnotationEffectiveStatement statement();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends AnnotationArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}