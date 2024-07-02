/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

class DataValidationFailedExceptionTest {
    @Test
    void dataValidationFailedExceptionTest() {
        final var testObj = new TestClass();
        final var dataValidationFailedException = new DataValidationFailedException(TestClass.class, testObj, "test");
        assertEquals(testObj, dataValidationFailedException.getPath());
        assertSame(TestClass.class, dataValidationFailedException.getPathType());
    }

    private static final class TestClass implements HierarchicalIdentifier<TestClass> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final TestClass other) {
            return false;
        }
    }
}
