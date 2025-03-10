/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
module org.opendaylight.mdsal.eos.binding.api {
    exports org.opendaylight.mdsal.eos.binding.api;

    requires transitive com.google.common;
    requires transitive org.opendaylight.mdsal.eos.common.api;
    requires transitive org.opendaylight.yangtools.binding.spec;

    requires org.opendaylight.mdsal.model.general.entity;

    // Annotations
    requires static org.osgi.annotation.bundle;
}
