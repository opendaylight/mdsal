/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

/**
 * Implementation of EntityOwnershipCandidateRegistration whose instances are returned from the
 * {@link BindingDOMEntityOwnershipServiceAdapter}.
 *
 * @author Thomas Pantelis
 */
class BindingEntityOwnershipCandidateRegistration extends AbstractObjectRegistration<Entity>
           implements EntityOwnershipCandidateRegistration {
    private final DOMEntityOwnershipCandidateRegistration domRegistration;

    BindingEntityOwnershipCandidateRegistration(DOMEntityOwnershipCandidateRegistration domRegistration,
            Entity entity) {
        super(entity);
        this.domRegistration = Preconditions.checkNotNull(domRegistration);
    }

    @Override
    protected void removeRegistration() {
        domRegistration.close();
    }
}