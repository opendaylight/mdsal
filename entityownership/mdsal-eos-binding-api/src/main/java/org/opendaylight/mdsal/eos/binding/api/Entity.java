/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.EntityKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link GenericEntity}.
 *
 * @author Thomas Pantelis
 */
@Beta
public class Entity extends GenericEntity<InstanceIdentifier<?>> {
    private static final long serialVersionUID = 1L;

    /** Constructs an instance.
     *
     * @param type the entity type
     * @param id the entity id.
     */
    public Entity(@Nonnull final String type, @Nonnull final InstanceIdentifier<?> id) {
        super(type, id);
    }

    /**
     * Construct an Entity with an with a name. The general-entity schema is used to construct the
     * InstanceIdentifier.
     *
     * @param type the type of the entity
     * @param entityName the name of the entity used to construct a general-entity InstanceIdentifier
     */
    public Entity(@Nonnull String type, @Nonnull String entityName) {
        super(type, InstanceIdentifier.builder(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                mdsal.core.general.entity.rev150930.Entity.class,
                    new EntityKey(Preconditions.checkNotNull(entityName, "entityName should not be null"))).build());
    }
}
