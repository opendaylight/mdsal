/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

public class DataValidationFailedExceptionTest {

    @Test(expected = DataValidationFailedException.class)
    public void dataValidationFailedExceptionTest() throws Exception {
        final TestClass testClass = new TestClass();
        final DataValidationFailedException dataValidationFailedException =
                new DataValidationFailedException(TestClass.class, testClass, "test");

        assertEquals(testClass, dataValidationFailedException.getPath());
        assertEquals(TestClass.class, dataValidationFailedException.getPathType());

        throw dataValidationFailedException;
    }

    private static final class TestClass implements HierarchicalIdentifier<TestClass> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final TestClass other) {
            return false;
        }
    }
}
