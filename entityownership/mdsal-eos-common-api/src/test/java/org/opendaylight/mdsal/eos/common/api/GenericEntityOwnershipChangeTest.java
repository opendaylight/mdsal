/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;

public class GenericEntityOwnershipChangeTest {

    @Test
    public void basicTest() throws Exception {
        final GenericEntity genericEntity = mock(GenericEntity.class);
        doReturn("testEntity").when(genericEntity).toString();
        final GenericEntityOwnershipChange genericEntityOwnershipChange =
                new GenericEntityOwnershipChange(genericEntity, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);

        assertEquals(genericEntity, genericEntityOwnershipChange.getEntity());
        assertEquals(EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, genericEntityOwnershipChange.getState());
        assertFalse(genericEntityOwnershipChange.inJeopardy());
        assertTrue(genericEntityOwnershipChange.toString().contains("testEntity"));
    }
}