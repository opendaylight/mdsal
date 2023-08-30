/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DataStoreUnavailableExceptionTest {
    @Test
    void dataStoreUnavailableExceptionTest() {
        final var ex = new DataStoreUnavailableException("test", null);
        assertEquals("test", ex.getMessage());
        assertNull(ex.getCause());
    }
}