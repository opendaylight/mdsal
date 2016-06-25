/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AbstractDOMStoreTransactionTest extends AbstractDOMStoreTransaction {

    private static final String IDENTIFIER = "testIdentifier";

    @Test
    public void basicTest() throws Exception {
        assertEquals(IDENTIFIER, this.getIdentifier());
        assertTrue(this.toString().contains(IDENTIFIER));
        assertNull(this.getDebugContext());
    }

    public AbstractDOMStoreTransactionTest() {
        super(IDENTIFIER);
    }

    @Override
    public void close() {
        // NOOP
    }
}