/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServiceGroupIdentifierTest {
    @Test
    void rejectEmptyValue() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> new ServiceGroupIdentifier(""));
        assertEquals("Value must not be blank", ex.getMessage());
    }

    @Test
    void rejectNullValue() {
        assertThrows(NullPointerException.class, () -> new ServiceGroupIdentifier(null));
    }
}
