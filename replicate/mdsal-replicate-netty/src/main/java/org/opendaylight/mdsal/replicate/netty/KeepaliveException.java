/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import java.io.Serial;

final class KeepaliveException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    KeepaliveException(final int missedKeepalives) {
        super("Keepalive Exchange Failed - missed " + missedKeepalives + " pings");
    }
}
