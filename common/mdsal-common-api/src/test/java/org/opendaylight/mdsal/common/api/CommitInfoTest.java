/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class CommitInfoTest {
    @Test
    void testEmpty() {
        assertSame(CI.EMPTY, CommitInfo.empty());
    }

    @Test
    void testEmptyFluentFuture() {
        assertSame(CI.EMPTY_FUTURE, CommitInfo.emptyFluentFuture());
    }
}
