/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.binding.util {
    exports org.opendaylight.mdsal.binding.util;

    requires transitive org.opendaylight.mdsal.binding.api;
    requires transitive org.opendaylight.mdsal.common.api;
    requires transitive org.opendaylight.yang.gen.ietf.datastores.rfc8342;
    requires org.opendaylight.mdsal.binding.spi;
    requires org.opendaylight.yangtools.util;
    requires org.slf4j;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires static transitive javax.inject;
    requires static com.github.spotbugs.annotations;
}
