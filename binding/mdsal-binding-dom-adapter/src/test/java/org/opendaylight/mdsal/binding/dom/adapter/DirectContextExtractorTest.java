/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.ContextReferenceExtractor.Direct;
import org.opendaylight.yang.gen.v1.urn.yang.foo.rev160101.BooleanContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DirectContextExtractorTest {
    private static final InstanceIdentifier<?> INSTANCE_IDENTIFIER = InstanceIdentifier.create(BooleanContainer.class);
    private static final String EXCEPTION_TEXT = "testException";

    @Test
    public void basicTest() throws Exception  {
        final var referenceExtractor = Direct.create(
            getClass().getDeclaredMethod("testMethod", BooleanContainer.class));
        assertEquals(INSTANCE_IDENTIFIER, referenceExtractor.extract(mock(BooleanContainer.class)));

        assertEquals(EXCEPTION_TEXT,
            assertThrows(NullPointerException.class, () -> referenceExtractor.extract(null)).getMessage());
    }

    public static InstanceIdentifier<?> testMethod(final BooleanContainer data) {
        if (data == null) {
            throw new NullPointerException(EXCEPTION_TEXT);
        }
        return INSTANCE_IDENTIFIER;
    }
}