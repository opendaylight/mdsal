/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

public interface BoxableType {

    /**
     * Check if generated type is suitable for boxing.
     *
     * @return true if generated type is suitable for boxing.
     */
    default boolean isSuitableForBoxing() {
        return false;
    }

    /**
     * Returns the parent type if Generated Type is defined as enclosing type, otherwise returns <code>null</code>.
     *
     * @return the parent type if Generated Type is defined as enclosing type, otherwise returns <code>null</code>
     */
    default JavaTypeName getParentType() {
        return null;
    }
}
