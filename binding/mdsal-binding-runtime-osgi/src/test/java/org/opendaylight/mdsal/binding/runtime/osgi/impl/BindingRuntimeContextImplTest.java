/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Dictionary;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.osgi.framework.Constants;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingRuntimeContextImplTest {
    @Mock
    private BindingRuntimeContext context;

    @Test
    public void testProps() {
        final long generation = ThreadLocalRandom.current().nextLong();
        final Integer expectedRanking = generation > Integer.MAX_VALUE || generation < 0
                ? Integer.MAX_VALUE : (int) generation;

        final Dictionary<String, ?> dict = BindingRuntimeContextImpl.props(generation, context);
        assertEquals(expectedRanking, dict.get(Constants.SERVICE_RANKING));
        assertEquals(generation, dict.get(BindingRuntimeContextImpl.GENERATION));
        assertSame(context, dict.get(BindingRuntimeContextImpl.DELEGATE));
        assertEquals(3, dict.size());
    }
}
