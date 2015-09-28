/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.clustering;

import org.opendaylight.mdsal.binding.api.clustering.Entity;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipCandidateRegistration;
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
        this.domRegistration = domRegistration;
    }

    @Override
    protected void removeRegistration() {
        domRegistration.close();
    }
}