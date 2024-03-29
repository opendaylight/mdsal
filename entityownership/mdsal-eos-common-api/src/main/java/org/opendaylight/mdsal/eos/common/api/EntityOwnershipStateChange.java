/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Enumerates the ownership change states for an entity.
 *
 * @author Thomas Pantelis
 */
public enum EntityOwnershipStateChange {
    /**
     * The local process instance has been granted ownership.
     */
    LOCAL_OWNERSHIP_GRANTED(false, true, true),

    /**
     * The local process instance has lost ownership and another process instance is now the owner.
     */
    LOCAL_OWNERSHIP_LOST_NEW_OWNER(true, false, true),

    /**
     * The local process instance has lost ownership and there are no longer any candidates for the entity and
     * thus has no owner.
     */
    LOCAL_OWNERSHIP_LOST_NO_OWNER(true, false, false),

    /**
     * The local process instance ownership has not changed but some other aspect has changed (for example inJeopardy).
     */
    LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE(true, true, true),

    /**
     * Entity ownership has transitioned to another process instance and this instance was not the previous owner.
     */
    REMOTE_OWNERSHIP_CHANGED(false, false, true),

    /**
     * A remote process instance has lost ownership and there are no longer any candidates for the entity and
     * thus has no owner.
     */
    REMOTE_OWNERSHIP_LOST_NO_OWNER(false, false, false);

    private static final ImmutableMap<Key, EntityOwnershipStateChange> BY_KEY;

    static {
        final var builder = ImmutableMap.<Key, EntityOwnershipStateChange>builder();
        for (var e : values()) {
            builder.put(new Key(e.wasOwner, e.isOwner, e.hasOwner), e);
        }
        BY_KEY = builder.build();
    }

    private final boolean wasOwner;
    private final boolean isOwner;
    private final boolean hasOwner;

    EntityOwnershipStateChange(final boolean wasOwner, final boolean isOwner, final boolean hasOwner) {
        this.wasOwner = wasOwner;
        this.isOwner = isOwner;
        this.hasOwner = hasOwner;
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
        return name() + " [wasOwner=" + wasOwner + ", isOwner=" + isOwner + ", hasOwner=" + hasOwner + "]";
    }

    public static @NonNull EntityOwnershipStateChange from(final boolean wasOwner, final boolean isOwner,
            final boolean hasOwner) {
        final var state = BY_KEY.get(new Key(wasOwner, isOwner, hasOwner));
        if (state != null) {
            return state;
        }
        throw new IllegalArgumentException("Invalid combination of wasOwner: %s, isOwner: %s, hasOwner: %s".formatted(
            wasOwner, isOwner, hasOwner));
    }

    private record Key(boolean wasOwner, boolean isOwner, boolean hasOwner) {
        // Nothing else
    }
}
