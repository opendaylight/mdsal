/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.rfc8294.netty {
    exports org.opendaylight.mdsal.rfc8294.netty;

    requires transitive io.netty.buffer;
    requires transitive org.opendaylight.yangtools.yang.common;
    requires transitive org.opendaylight.yang.gen.ietf.routing.types.rfc8294;

    // Annotations
    requires static org.eclipse.jdt.annotation;
}

