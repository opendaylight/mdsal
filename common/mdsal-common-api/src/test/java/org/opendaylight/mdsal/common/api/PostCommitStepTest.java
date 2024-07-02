/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.mdsal.common.api.PostPreCommitStep.NOOP_COMMIT_FUTURE;

import org.junit.jupiter.api.Test;

class PostCommitStepTest {
    @Test
    void preCommitTest() {
        final var noop = PostCanCommitStep.NOOP;
        assertEquals(ThreePhaseCommitStep.NOOP_ABORT_FUTURE ,noop.abort());
        assertEquals(PostPreCommitStep.NOOP_FUTURE, noop.preCommit());
    }

    @Test
    void canCommitTest() {
        final var noop = PostPreCommitStep.NOOP;
        assertEquals(ThreePhaseCommitStep.NOOP_ABORT_FUTURE ,noop.abort());
        assertEquals(NOOP_COMMIT_FUTURE, noop.commit());
    }
}