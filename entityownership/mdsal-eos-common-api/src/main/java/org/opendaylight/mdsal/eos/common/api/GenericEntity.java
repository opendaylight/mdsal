/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Path;

/**
 * A clustered Entity is something which is shared by multiple applications across a cluster. An Entity has a type
 * and an identifier.
 *
 * <p>
 * The type describes the type of the Entity where examples of a type maybe "openflow" or "netconf"
 * etc. An Entity type could be tied to how exactly an application shares and "owns" an entity. For example we may want
 * an application which deals with the openflow entity to be assigned ownership of that entity based on a first come
 * first served basis. On the other hand for netconf entity types we may want applications to gain ownership based on
 * a load balancing approach. While this mechanism of assigning a ownership acquisition strategy is not finalized the
 * intention is that the entity type will play a role in determining the strategy and thus should be put in place.
 *
 * <p>
 * The identifier is an instance identifier path. The reason for the choice of instance identifier path is because it
 * can easily be used to represent a data node. For example an inventory node represents a shared entity and it is best
 * referenced by its instance identifier path if the inventory node is stored in the data store.
 *
 * <p>
 * Note that an entity identifier must conform to a valid yang schema. If there is no existing yang schema to
 * represent an entity, the general-entity yang model can be used.
 *
 * <p>
 *
 * @author Thomas Pantelis
 *
 * @param <T> the entity identifier type
 */
public class GenericEntity<T extends Path<T>> implements Serializable, Identifiable<T> {
    private static final long serialVersionUID = 1L;

    private final @NonNull String type;
    private final @NonNull T id;

    protected GenericEntity(@NonNull final String type, @NonNull final T id) {
        this.type = requireNonNull(type, "type should not be null");
        this.id = requireNonNull(id, "id should not be null");
    }

    /**
     * Gets the id of the entity.
     * @return the id.
     */
    @Override
    public final @NonNull T getIdentifier() {
        return id;
    }

    /**
     * Gets the type of the entity.
     * @return the type.
     */
    public final @NonNull String getType() {
        return type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final GenericEntity<?> entity = (GenericEntity<?>) obj;
        return id.equals(entity.id) && type.equals(entity.type);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [type=" + type + ", id=" + id + "]";
    }
}
