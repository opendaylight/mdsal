/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListener;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Path;

/**
 * Abstract base class for an {@link GenericEntityOwnershipListenerRegistration}.
 *
 * @param <P> the instance identifier path type
 * @param <L> the GenericEntityOwnershipListener type
 */
public abstract class AbstractGenericEntityOwnershipListenerRegistration<P extends Path<P>,
    L extends GenericEntityOwnershipListener<P, ? extends GenericEntityOwnershipChange<P, ? extends GenericEntity<P>>>>
        extends AbstractObjectRegistration<L>
        implements GenericEntityOwnershipListenerRegistration<P, L> {

    private final @NonNull String entityType;

    protected AbstractGenericEntityOwnershipListenerRegistration(@NonNull final L instance,
            @NonNull final String entityType) {
        super(instance);
        this.entityType = requireNonNull(entityType, "entityType cannot be null");
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

}
