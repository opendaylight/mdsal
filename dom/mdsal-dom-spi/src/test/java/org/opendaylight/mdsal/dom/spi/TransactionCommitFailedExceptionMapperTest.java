/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

class TransactionCommitFailedExceptionMapperTest {
    private final TransactionCommitFailedExceptionMapper mapper = TransactionCommitFailedExceptionMapper.create("test");

    @Test
    void create() {
        final var cause = new Throwable();
        final var ex = assertInstanceOf(TransactionCommitFailedException.class,
            mapper.newWithCause("test_cause", cause));
        assertSame(cause, ex.getCause());
    }
}