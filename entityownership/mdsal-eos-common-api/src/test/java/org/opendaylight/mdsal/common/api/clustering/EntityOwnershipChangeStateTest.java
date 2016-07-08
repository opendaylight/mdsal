/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for EntityOwnershipChangeState.
 *
 * @author Thomas Pantelis
 */
public class EntityOwnershipChangeStateTest {

    @Test
    public void testFromWithValid() throws Exception {
        assertEquals("from(false, true, true)", EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED,
                EntityOwnershipChangeState.from(false, true, true));
        assertEquals("from(true, false, true)", EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NEW_OWNER,
                EntityOwnershipChangeState.from(true, false, true));
        assertEquals("from(true, false, false)", EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER,
                EntityOwnershipChangeState.from(true, false, false));
        assertEquals("from(false, false, true)", EntityOwnershipChangeState.REMOTE_OWNERSHIP_CHANGED,
                EntityOwnershipChangeState.from(false, false, true));
        assertEquals("from(false, false, false)", EntityOwnershipChangeState.REMOTE_OWNERSHIP_LOST_NO_OWNER,
                EntityOwnershipChangeState.from(false, false, false));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromWithInvalidFalseTrueFalse() {
        EntityOwnershipChangeState.from(false, true, false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromWithInvalidTrueTrueFalse() {
        EntityOwnershipChangeState.from(true, true, false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromWithInvalidTrueTrueTrue() {
        EntityOwnershipChangeState.from(true, true, true);
    }

    @Test
    public void basicTest() throws Exception {
        EntityOwnershipChangeState entityOwnershipChangeState = EntityOwnershipChangeState.from(false, true, true);
        assertTrue(entityOwnershipChangeState.hasOwner());
        assertTrue(entityOwnershipChangeState.isOwner());
        assertFalse(entityOwnershipChangeState.wasOwner());
        assertTrue(entityOwnershipChangeState.toString().matches(".*false.*true.*true.*"));
    }
}