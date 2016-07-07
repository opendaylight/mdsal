/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.api;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class DOMEntityOwnershipChangeTest {
    private static final String ENTITY_TYPE = "type";
    private static final YangInstanceIdentifier ID = YangInstanceIdentifier.of(QName.create("test"));
    private static final DOMEntity ENTITY = new DOMEntity(ENTITY_TYPE, ID);

    @Test
    public void createTest() {
        assertNotNull(new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED));
        assertNotNull(new DOMEntityOwnershipChange(ENTITY, EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED, false));
    }
}