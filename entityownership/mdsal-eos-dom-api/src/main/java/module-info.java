/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.eos.dom.api {
    exports org.opendaylight.mdsal.eos.dom.api;

    requires transitive org.opendaylight.mdsal.eos.common.api;
    requires transitive org.opendaylight.yangtools.yang.data.api;

    // Annotations
    requires static transitive org.eclipse.jdt.annotation;
    requires org.osgi.annotation.bundle;
}
