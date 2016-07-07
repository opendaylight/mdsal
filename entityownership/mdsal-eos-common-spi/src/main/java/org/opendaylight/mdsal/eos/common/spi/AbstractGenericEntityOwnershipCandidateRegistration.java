/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.eos.common.spi;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Path;

/**
 * Abstract base class for an EntityOwnershipCandidateRegistration.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 */
public abstract class AbstractGenericEntityOwnershipCandidateRegistration<P extends Path<P>, E extends GenericEntity<P>>
        extends AbstractObjectRegistration<E>
        implements GenericEntityOwnershipCandidateRegistration<P, E> {

    protected AbstractGenericEntityOwnershipCandidateRegistration(@Nonnull final E entity) {
        super(Preconditions.checkNotNull(entity, "entity cannot be null"));
    }
}

