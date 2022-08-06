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

/**
 * The archetype of an enum generated for a particular {@code type bits} statement statement.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface BitsArchetype extends TypeObjectArchetype {
    @Override
    default Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    // corresponds to
    //   statement().streamEffectiveSubstatements(BitEffectiveStatement.class).map( create Field )
    List<BitsField> fields();

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends BitsArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}