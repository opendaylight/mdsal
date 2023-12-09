/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenericEntityOwnershipChangeTest {
    @Mock
    private GenericEntity<?> genericEntity;

    @Test
    void basicTest() {
        doReturn("testEntity").when(genericEntity).toString();
        final var genericEntityOwnershipChange = new EntityOwnershipChange<>(genericEntity,
            EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED);

        assertEquals(genericEntity, genericEntityOwnershipChange.getEntity());
        assertEquals(EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, genericEntityOwnershipChange.getState());
        assertFalse(genericEntityOwnershipChange.inJeopardy());
        assertTrue(genericEntityOwnershipChange.toString().contains("testEntity"));
    }
}