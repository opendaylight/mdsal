/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EntityOwnershipStateTest {

    @Test
    public void fromTest() throws Exception {
        assertEquals(EntityOwnershipState.NO_OWNER, EntityOwnershipState.from(false, false));
        assertEquals(EntityOwnershipState.IS_OWNER, EntityOwnershipState.from(true, false));
        assertEquals(EntityOwnershipState.OWNED_BY_OTHER, EntityOwnershipState.from(false, true));
    }
}