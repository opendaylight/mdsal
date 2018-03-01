/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.testutil;

import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;

/**
 * Fake EntityOwnershipCandidateRegistration suitable for non-clustered component tests.
 */
public abstract class TestEntityOwnershipCandidateRegistration implements EntityOwnershipCandidateRegistration {

    // Do nothing
    @Override
    public void close() {
    }

}
