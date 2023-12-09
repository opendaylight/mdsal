/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for EntityOwnershipStateChange.
 *
 * @author Thomas Pantelis
 */
public class EntityOwnershipStateChangeTest {
    @Test
    public void testFromWithValid() {
        assertEquals("from(false, true, true)", EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED,
                EntityOwnershipStateChange.from(false, true, true));
        assertEquals("from(true, false, true)", EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NEW_OWNER,
                EntityOwnershipStateChange.from(true, false, true));
        assertEquals("from(true, false, false)", EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NO_OWNER,
                EntityOwnershipStateChange.from(true, false, false));
        assertEquals("from(false, false, true)", EntityOwnershipStateChange.REMOTE_OWNERSHIP_CHANGED,
                EntityOwnershipStateChange.from(false, false, true));
        assertEquals("from(false, false, false)", EntityOwnershipStateChange.REMOTE_OWNERSHIP_LOST_NO_OWNER,
                EntityOwnershipStateChange.from(false, false, false));
        assertEquals("from(true, true, true)", EntityOwnershipStateChange.LOCAL_OWNERSHIP_RETAINED_WITH_NO_CHANGE,
                EntityOwnershipStateChange.from(true, true, true));
    }

    @Test
    public void testFromWithInvalidFalseTrueFalse() {
        assertThrows(IllegalArgumentException.class, () -> EntityOwnershipStateChange.from(false, true, false));
    }

    @Test
    public void testFromWithInvalidTrueTrueFalse() {
        assertThrows(IllegalArgumentException.class, () -> EntityOwnershipStateChange.from(true, true, false));
    }

    @Test
    public void basicTest() {
        final var entityOwnershipChangeState = EntityOwnershipStateChange.from(false, true, true);
        assertTrue(entityOwnershipChangeState.hasOwner());
        assertTrue(entityOwnershipChangeState.isOwner());
        assertFalse(entityOwnershipChangeState.wasOwner());
        assertTrue(entityOwnershipChangeState.toString().matches(".*false.*true.*true.*"));
    }
}
