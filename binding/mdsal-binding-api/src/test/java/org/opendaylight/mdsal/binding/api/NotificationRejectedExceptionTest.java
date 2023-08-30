/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotificationRejectedExceptionTest {
    @Test
    void constructWithCauseTest() throws Exception {
        assertThrows(NotificationRejectedException.class, () -> {
            throw new NotificationRejectedException("test", new Throwable());
        });
    }

    @Test
    void constructTest() throws Exception {
        assertThrows(NotificationRejectedException.class, () -> {
            throw new NotificationRejectedException("test");
        });
    }
}