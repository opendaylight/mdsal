/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@Beta
@NonNullByDefault
public sealed interface JavaConstruct {

    non-sealed interface Interface extends JavaConstruct {

        ImmutableSet<Type> extendsTypes();
    }

    sealed interface ClassLike extends JavaConstruct {

        @Nullable Type extendsType();

        ImmutableSet<Type> implementsTypes();
    }

    // Note: we do not want to model 'static' -- none of our classes are designed to be non-static, therefore it being
    //       static is implied to all nested types
    non-sealed interface Class extends ClassLike {
        /**
         * Completeness of the class. A class can be abstract or final or non-final (default in-between)
         */
        enum Completeness {
            ABSTRACT,
            DEFAULT,
            FINAL;
        }

        Completeness completeness();
    }

    non-sealed interface Enum extends ClassLike {

    }

    non-sealed interface Record extends ClassLike {
        // Not used right now
    }
}
