/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Base interface for all run-time type information about a particular {@link GeneratedType}.
 */
@Beta
public interface RuntimeType<S extends EffectiveStatement<?, ?>, T extends GeneratedType>
        extends Identifiable<JavaTypeName>, Immutable {
    @Override
    default JavaTypeName getIdentifier() {
        return bindingType().getIdentifier();
    }

    @NonNull T bindingType();

    @NonNull S schema();
}
