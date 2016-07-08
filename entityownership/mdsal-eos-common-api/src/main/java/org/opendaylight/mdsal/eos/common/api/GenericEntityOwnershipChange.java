/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
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
    private final E entity;
    private final EntityOwnershipChangeState state;
    private final boolean inJeopardy;

    public GenericEntityOwnershipChange(@Nonnull final E entity, @Nonnull final EntityOwnershipChangeState state) {
        this(entity, state, false);
    }

    public GenericEntityOwnershipChange(@Nonnull final E entity, @Nonnull final EntityOwnershipChangeState state,
            final boolean inJeopardy) {
        this.entity = Preconditions.checkNotNull(entity, "entity can't be null");
        this.state = Preconditions.checkNotNull(state, "state can't be null");
        this.inJeopardy = inJeopardy;
    }

    /**
     * Returns the entity whose ownership status changed.
     * @return the entity
     */
    @Nonnull public E getEntity() {
        return entity;
    }

    /**
     * Returns the ownership change state.
     * @return an EntityOwnershipChangeState enum
     */
    @Nonnull public EntityOwnershipChangeState getState() {
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
        return getClass().getSimpleName() + " [entity=" + entity + ", state=" + state + ", inJeopardy=" + inJeopardy + "]";
    }
}
