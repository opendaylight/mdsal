/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class FutureSchemaTest {
    @Test
    public void basicTest() {
        final FutureSchema futureSchema = FutureSchema.create(0, TimeUnit.MICROSECONDS, true);
        assertNotNull(futureSchema);
        assertFalse(futureSchema.waitForSchema(QNameModule.of("test")));
        assertFalse(futureSchema.waitForSchema(ImmutableSet.of()));
        assertEquals(0, futureSchema.getDuration());
        assertEquals(TimeUnit.MICROSECONDS, futureSchema.getUnit());
    }
}