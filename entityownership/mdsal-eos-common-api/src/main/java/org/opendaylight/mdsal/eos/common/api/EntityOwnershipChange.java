/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A DTO that encapsulates an ownership change for an entity.
 *
 * @param <E> the {@link GenericEntity} type
 * @author Thomas Pantelis
 */
public final class EntityOwnershipChange<E extends GenericEntity<?>> {
    private final @NonNull E entity;
    private final @NonNull EntityOwnershipStateChange state;
    private final boolean inJeopardy;

    public EntityOwnershipChange(final @NonNull E entity, final @NonNull EntityOwnershipStateChange state) {
        this(entity, state, false);
    }

    public EntityOwnershipChange(final @NonNull E entity, final @NonNull EntityOwnershipStateChange state,
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
     * @return an EntityOwnershipStateChange enum
     */
    public @NonNull EntityOwnershipStateChange getState() {
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
        return MoreObjects.toStringHelper(this)
            .add("entity", entity)
            .add("state", state)
            .add("inJeopardy", inJeopardy)
            .toString();
    }
}
