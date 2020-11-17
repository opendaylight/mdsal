/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementing this interface allows an object to hold information which are
 * essential for generating methods for supporting autoboxing.
 */
public interface BoxableType {

    /**
     * Check if generated type is suitable for boxing.
     *
     * @implSpec
     *      The default implementation returns {@code false}.
     *
     * @return true if generated type is suitable for boxing.
     */
    default boolean isSuitableForBoxing() {
        return false;
    }

    /**
     * Returns the parent type if Generated Type is defined as enclosing type, otherwise returns {@code null}.
     *
     * @implSpec
     *      The default implementation returns {@code null}.
     *
     * @return the parent type if Generated Type is defined as enclosing type, otherwise returns {@code null}.
     */
    @Nullable default JavaTypeName getParentType() {
        return null;
    }
}
