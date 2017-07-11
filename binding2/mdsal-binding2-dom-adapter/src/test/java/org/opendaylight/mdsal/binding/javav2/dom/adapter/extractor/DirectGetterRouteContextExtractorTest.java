/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

public class DirectGetterRouteContextExtractorTest {

    private static final InstanceIdentifier INSTANCE_IDENTIFIER = InstanceIdentifier.create(TreeNode.class);
    private static final String EXCEPTION_TEXT = "testException";

    @Test
    public void basicTest() throws Exception {
        final Method testMthd = this.getClass().getDeclaredMethod("testMethod", TreeNode.class);
        testMthd.setAccessible(true);
        final ContextReferenceExtractor referenceExtractor = DirectGetterRouteContextExtractor.create(testMthd);
        assertEquals(testMethod(mock(TreeNode.class)), referenceExtractor.extract(mock(TreeNode.class)));

        try {
            referenceExtractor.extract(null);
            fail("Expected exception");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().equals(EXCEPTION_TEXT));
        }
    }

    private static InstanceIdentifier testMethod(TreeNode data) {
        if (data == null) {
            throw new NullPointerException(EXCEPTION_TEXT);
        }
        return INSTANCE_IDENTIFIER;
    }
}