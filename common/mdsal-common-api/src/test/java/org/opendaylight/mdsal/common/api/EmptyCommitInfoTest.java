/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;

class EmptyCommitInfoTest {
    @Test
    void testFuture() throws Exception {
        final var future = CI.EMPTY_FUTURE;
        assertTrue(future.isDone());
        assertSame(CI.EMPTY, Futures.getDone(future));
    }
}
