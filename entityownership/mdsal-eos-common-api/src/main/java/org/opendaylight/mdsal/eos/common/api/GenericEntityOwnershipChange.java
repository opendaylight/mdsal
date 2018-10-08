/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Path;

/**
 * A DTO that encapsulates an ownership change for an entity.
 *
 * @author Thomas Pantelis
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 */
public class GenericEntityOwnershipChange<P extends Path<P>, E extends GenericEntity<P>> {
    private final @NonNull E entity;
    private final @NonNull EntityOwnershipChangeState state;
    private final boolean inJeopardy;

    public GenericEntityOwnershipChange(final @NonNull E entity, final @NonNull EntityOwnershipChangeState state) {
        this(entity, state, false);
    }

    public GenericEntityOwnershipChange(final @NonNull E entity, final @NonNull EntityOwnershipChangeState state,
            final boolean inJeopardy) {
        this.entity = requireNonNull(entity, "entity can't be null");
        this.state = requireNonNull(state, "state can't be null");
        this.inJeopardy = inJeopardy;
    }

    /**
     * Returns the entity whose ownership status changed.
     * @return the entity
     */
    public @NonNull E getEntity() {
        return entity;
    }

    /**
     * Returns the ownership change state.
     * @return an EntityOwnershipChangeState enum
     */
    public @NonNull EntityOwnershipChangeState getState() {
        return state;
    }

    /**
     * Returns the current jeopardy state. When in a jeopardy state, the values from other methods may potentially
     * be out of date.
     *
     * @return true if the local node is in a jeopardy state. If false, the reported information is accurate.
     */
    public boolean inJeopardy() {
        return inJeopardy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [entity=" + entity + ", state=" + state
                + ", inJeopardy=" + inJeopardy + "]";
    }
}
