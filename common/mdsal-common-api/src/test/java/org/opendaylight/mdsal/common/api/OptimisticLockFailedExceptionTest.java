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
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;

class OptimisticLockFailedExceptionTest {
    @Test
    void optimisticLockFailedExceptionTest() {
        final var ex = new OptimisticLockFailedException("test");
        assertEquals("test", ex.getMessage());
        final var errors = ex.getErrorList();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorType.APPLICATION, error.getErrorType());
        assertEquals(ErrorTag.RESOURCE_DENIED, error.getTag());
        assertNull(error.getApplicationTag());
        assertNull(error.getInfo());
        assertNull(error.getCause());
    }
}