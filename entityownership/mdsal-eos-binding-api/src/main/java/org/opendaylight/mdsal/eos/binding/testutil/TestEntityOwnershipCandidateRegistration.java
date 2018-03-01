/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.testutil;

import com.google.common.base.Optional;
import org.mockito.Mockito;
import org.opendaylight.mdsal.eos.binding.api.*;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipCandidateRegistration;

import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.realOrException;

/**
 * Fake EntityOwnershipService suitable for non-clustered component tests.
 *
 */
public abstract class TestEntityOwnershipCandidateRegistration implements EntityOwnershipCandidateRegistration {

    // Do nothing
    public void close() {
    }

}
