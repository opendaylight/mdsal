/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

/**
 * Generated Property extends {@link TypeMember} interface with additional information about fields (and other members)
 * declared in Java Transfer Objects (or any java classes) and their access counterparts (getters and setters).
 *
 * @see TypeMember
 */
// FIXME: 7.0.0: this interface (and others) need to be refactored:
//               - getValue() is pretty much unused and its semantics are undefined
//               - isReadOnly() is not related to getValue() and is not used together
//               - nullifyEmpty() is applicable only to collection types and implies non-read-only and without value
//               - this is misused by Builder spec :(
public interface GeneratedProperty extends TypeMember {

    String getValue();

    /**
     * Returns <code>true</code> if the property is declared as read-only. If this {@code true} the property should be
     * generated with only a getter.
     *
     * @return {@code true<} if the property is declared as read-only.
     */
    boolean isReadOnly();

    /**
     * Returns indication whether the value should be squashed from empty collection to a null. This property is valid
     * only if {@link #getReturnType()} results in a well-known collection type: List or Map.
     *
     * @return True if empty collections should be turned to nulls
     */
    boolean nullifyEmpty();
}
