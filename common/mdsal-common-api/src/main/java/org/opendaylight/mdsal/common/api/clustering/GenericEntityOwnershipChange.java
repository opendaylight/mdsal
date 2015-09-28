/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering;

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
    private final boolean wasOwner;
    private final boolean isOwner;
    private final boolean hasOwner;

    public GenericEntityOwnershipChange(@Nonnull E entity, boolean wasOwner, boolean isOwner,
            boolean hasOwner) {
        this.entity = Preconditions.checkNotNull(entity, "entity can't be null");
        this.wasOwner = wasOwner;
        this.isOwner = isOwner;
        this.hasOwner = hasOwner;
    }

    /**
     * Returns the entity whose ownership status changed.
     * @return the entity
     */
    @Nonnull public E getEntity() {
        return entity;
    }

    /**
     * Returns the previous ownership status of the entity for this process instance.
     * @return true if this process was the owner of the entity at the time this notification was generated
     */
    public boolean wasOwner() {
        return wasOwner;
    }

    /**
     * Returns the current ownership status of the entity for this process instance.
     * @return true if this process is now the owner of the entity
     */
    public boolean isOwner() {
        return isOwner;
    }

    /**
     * Returns the current ownership status of the entity across all process instances.
     * @return true if the entity has an owner which may or may not be this process. If false, then
     *         the entity has no candidates and thus no owner.
     */
    public boolean hasOwner() {
        return hasOwner;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [entity=" + entity + ", wasOwner=" + wasOwner + ", isOwner=" + isOwner
                + ", hasOwner=" + hasOwner + "]";
    }
}
