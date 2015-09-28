/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.clustering;

import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipChange;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link GenericEntityOwnershipChange}.
 *
 * @author Thomas Pantelis
 */
public class EntityOwnershipChange extends GenericEntityOwnershipChange<InstanceIdentifier<?>, Entity> {

    /**
     * {@inheritDoc}
     */
    public EntityOwnershipChange(@Nonnull Entity entity, boolean wasOwner, boolean isOwner,
            boolean hasOwner) {
        super(entity, wasOwner, isOwner, hasOwner);
    }
}
