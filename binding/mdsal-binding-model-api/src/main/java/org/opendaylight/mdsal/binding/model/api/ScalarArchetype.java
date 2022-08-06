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

/**
 * The archetype of a class generated for a particular {@code type} statement holding a single value.
 */
@DoNotMock
@Value.Immutable
@NonNullByDefault
public non-sealed interface ScalarArchetype extends TypeObjectArchetype {
    @Override
    default Class<JavaConstruct.Class> construct() {
        return JavaConstruct.Class.class;
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends ScalarArchetypeBuilder {
        Builder() {
            // Hidden on purpose
        }
    }
}