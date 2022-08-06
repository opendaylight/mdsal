/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Abstract base class for builders. Holds utility methods.
 */
abstract class AbstractBuilder implements Mutable {

    static final <T> @NonNull T checkSet(final T value, final String name) {
        if (value == null) {
            throw new IllegalStateException(name + " not set");
        }
        return value;
    }
}
