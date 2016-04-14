/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.clustering.GenericEntity;
import org.opendaylight.mdsal.common.api.clustering.impl.AbstractGenericEntityOwnershipCandidateRegistration;

public class AbstractGenericEntityOwnershipCandidateRegistrationTest
        extends AbstractGenericEntityOwnershipCandidateRegistration {

    @Test
    public void basicTest() {
        assertNotNull(this);
    }

    public AbstractGenericEntityOwnershipCandidateRegistrationTest() {
        super(mock(GenericEntity.class));
    }

    @Override
    protected void removeRegistration() {
        //NOOP
    }
}