/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AbstractDOMStoreTransactionTest extends AbstractDOMStoreTransaction<String> {
    private static final String IDENTIFIER = "testIdentifier";

    AbstractDOMStoreTransactionTest() {
        super(IDENTIFIER);
    }

    @Test
    void basicTest() throws Exception {
        assertEquals(IDENTIFIER, getIdentifier());
        assertTrue(toString().contains(IDENTIFIER));
        assertNull(getDebugContext());
    }

    @Override
    public void close() {
        // NOOP
    }
}